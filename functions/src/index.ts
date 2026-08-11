import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { google } from "googleapis";

admin.initializeApp();

const db = admin.firestore();

// Helpers for Server-Side Authorization
async function requireOwner(context: functions.https.CallableContext): Promise<void> {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Authentication required");
  }
  // Safe migration/claims check
  const uid = context.auth.uid;
  const token = context.auth.token;
  const userDoc = await db.collection("users").document(uid).get();
  const dbRole = userDoc.exists ? userDoc.data()?.role : null;

  if (token.owner !== true && dbRole !== "OWNER") {
    throw new functions.https.HttpsError("permission-denied", "Owner privileges required");
  }
}

async function requireAdminOrOwner(context: functions.https.CallableContext): Promise<void> {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Authentication required");
  }
  const uid = context.auth.uid;
  const token = context.auth.token;
  const userDoc = await db.collection("users").document(uid).get();
  const dbRole = userDoc.exists ? userDoc.data()?.role : null;

  if (token.owner !== true && token.admin !== true && dbRole !== "OWNER" && dbRole !== "ADMIN") {
    throw new functions.https.HttpsError("permission-denied", "Admin or Owner privileges required");
  }
}

// 1. Assign Custom Claims
export const assignCustomClaims = functions.https.onCall(async (data, context) => {
  await requireOwner(context);

  const { targetUid, role } = data;
  if (!targetUid || !role) {
    throw new functions.https.HttpsError("invalid-argument", "Missing targetUid or role");
  }

  const claims = role === "OWNER" ? { owner: true } : role === "ADMIN" ? { admin: true } : {};
  await admin.auth().setCustomUserClaims(targetUid, claims);

  // Write trust audit log
  await db.collection("activity_logs").add({
    actorUid: context.auth!.uid,
    actorRole: "OWNER",
    action: `Assigned custom claims: ${JSON.stringify(claims)} to user ${targetUid}`,
    targetUid,
    timestamp: admin.firestore.FieldValue.serverTimestamp(),
    result: "SUCCESS"
  });

  return { success: true, message: `Custom claims set to ${role} successfully` };
});

// 2. Grant Plan to User (Owner-Only)
export const grantPlanToUser = functions.https.onCall(async (data, context) => {
  await requireOwner(context);

  const { targetEmail, planName, durationMs } = data;
  if (!targetEmail || !planName || !durationMs) {
    throw new functions.https.HttpsError("invalid-argument", "Missing parameters");
  }

  const sanitizedEmail = targetEmail.trim().lowercase().replace("@", "_at_").replace(".", "_dot_");
  const purchaseId = "manual_server_" + Date.now();

  const entitlementMap = {
    planId: "manual",
    planName: planName,
    status: "ACTIVE",
    validFrom: Date.now(),
    validUntil: Date.now() + durationMs,
    benefits: "premium_content,ad_free,mock_tests",
    source: "OWNER",
    purchaseId: purchaseId,
    updatedAt: Date.now()
  };

  const batch = db.batch();
  const userRef = db.collection("users").document(sanitizedEmail);
  const entitlementRef = userRef.collection("entitlements").document("current");
  const historyRef = userRef.collection("entitlement_history").document(purchaseId);

  batch.set(entitlementRef, entitlementMap);
  batch.set(historyRef, entitlementMap);
  batch.update(userRef, { isPremium: true });

  await batch.commit();

  // Audit log
  await db.collection("activity_logs").add({
    actorUid: context.auth!.uid,
    actorRole: "OWNER",
    action: `Granted plan ${planName} to user ${targetEmail}`,
    targetUid: sanitizedEmail,
    timestamp: admin.firestore.FieldValue.serverTimestamp(),
    result: "SUCCESS"
  });

  return { success: true };
});

// 3. Google Play Purchase Verification & Entitlement Provisioning
export const verifyAndProvisionPurchase = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Authentication required");
  }

  const { purchaseToken, productId, packageName } = data;
  if (!purchaseToken || !productId || !packageName) {
    throw new functions.https.HttpsError("invalid-argument", "Missing verification parameters");
  }

  // Set up Google Play Developer API Client using Google service account key
  // Normally configured via Firebase config secrets.
  const authClient = new google.auth.GoogleAuth({
    scopes: ["https://www.googleapis.com/auth/androidpublisher"]
  });

  const playDeveloperApi = google.androidpublisher({
    version: "v3",
    auth: authClient
  });

  try {
    // 1. Query the Play Console for purchase details
    const response = await playDeveloperApi.purchases.products.get({
      packageName,
      productId,
      token: purchaseToken
    });

    const purchaseState = response.data.purchaseState; // 0 = purchased, 1 = canceled, 2 = pending
    if (purchaseState !== 0) {
      throw new functions.https.HttpsError("failed-precondition", "Purchase is not valid or was canceled");
    }

    const uid = context.auth.uid;
    const purchaseId = "play_" + purchaseToken.substring(0, 20);

    // 2. Replay Protection & Idempotency check via Firestore transaction
    const purchaseRef = db.collection("users").document(uid).collection("purchases").document(purchaseId);
    const entitlementRef = db.collection("users").document(uid).collection("entitlements").document("current");

    await db.runTransaction(async (transaction) => {
      const purchaseDoc = await transaction.get(purchaseRef);
      if (purchaseDoc.exists && (purchaseDoc.data()?.status === "COMPLETED" || purchaseDoc.data()?.status === "PROVISIONED")) {
        throw new functions.https.HttpsError("already-exists", "This purchase token has already been processed");
      }

      // Provision entitlement
      const entitlementMap = {
        planId: productId,
        planName: "Play subscription/product",
        status: "ACTIVE",
        validFrom: Date.now(),
        validUntil: Date.now() + 365 * 24 * 60 * 60 * 1000, // 1 Year default
        benefits: "premium_content,ad_free,mock_tests",
        source: "GOOGLE_PLAY",
        purchaseId: purchaseId,
        updatedAt: Date.now()
      };

      transaction.set(purchaseRef, {
        purchaseId,
        purchaseToken,
        productId,
        packageName,
        timestamp: Date.now(),
        status: "PROVISIONED"
      });
      transaction.set(entitlementRef, entitlementMap);
      transaction.update(db.collection("users").document(uid), { isPremium: true });
    });

    return { success: true, message: "Purchase verified and entitlement provisioned successfully" };

  } catch (err: any) {
    functions.logger.error("Play purchase verification failed", err);
    throw new functions.https.HttpsError("internal", err.message || "Purchase verification failure");
  }
});

// 4. Atomic Pending Request Approval
export const approvePendingRequest = functions.https.onCall(async (data, context) => {
  await requireAdminOrOwner(context);

  const { requestId } = data;
  if (!requestId) {
    throw new functions.https.HttpsError("invalid-argument", "Missing request ID");
  }

  const reqRef = db.collection("pending_requests").document(requestId);

  try {
    const result = await db.runTransaction(async (transaction) => {
      const reqDoc = await transaction.get(reqRef);
      if (!reqDoc.exists) {
        throw new functions.https.HttpsError("not-found", "Request not found");
      }

      const status = reqDoc.data()?.status;
      if (status !== "PENDING") {
        throw new functions.https.HttpsError("failed-precondition", `Request already in state ${status}`);
      }

      // Transition to PROCESSING atomically
      transaction.update(reqRef, { status: "PROCESSING" });
      return reqDoc.data();
    });

    // Execute actual execution inside function or handoff to backend
    // Finally update status to APPROVED
    await reqRef.update({ status: "APPROVED", resolvedBy: context.auth!.uid, resolvedAt: Date.now() });

    // Trust Audit Log
    await db.collection("activity_logs").add({
      actorUid: context.auth!.uid,
      actorRole: context.auth!.token.admin ? "ADMIN" : "OWNER",
      action: `Approved and processed request: ${requestId}`,
      targetUid: result?.targetId || "",
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      result: "SUCCESS"
    });

    return { success: true };
  } catch (err: any) {
    throw new functions.https.HttpsError("internal", err.message || "Approval process failed");
  }
});

// 5. Trusted User Blocking
export const toggleUserBlockState = functions.https.onCall(async (data, context) => {
  await requireOwner(context);

  const { targetUid, block } = data;
  if (!targetUid) {
    throw new functions.https.HttpsError("invalid-argument", "Missing targetUid");
  }

  // 1. Disable/Enable in Firebase Authentication
  await admin.auth().updateUser(targetUid, { disabled: block });

  // 2. Revoke Refresh Tokens to immediately invalidate active sessions
  if (block) {
    await admin.auth().revokeRefreshTokens(targetUid);
  }

  // 3. Update Firestore Document
  await db.collection("users").document(targetUid).update({
    role: block ? "BLOCKED" : "USER",
    isPremium: block ? false : admin.firestore.FieldValue.increment(0)
  });

  // 4. Audit Log
  await db.collection("activity_logs").add({
    actorUid: context.auth!.uid,
    actorRole: "OWNER",
    action: `${block ? "Blocked" : "Unblocked"} user UID: ${targetUid}`,
    targetUid,
    timestamp: admin.firestore.FieldValue.serverTimestamp(),
    result: "SUCCESS"
  });

  return { success: true };
});

// 6. Trusted User Deletion
export const deleteUserCompletely = functions.https.onCall(async (data, context) => {
  await requireOwner(context);

  const { targetUid } = data;
  if (!targetUid) {
    throw new functions.https.HttpsError("invalid-argument", "Missing targetUid");
  }

  // 1. Delete/Disable from Firebase Auth
  await admin.auth().deleteUser(targetUid);

  // 2. Invalidate refresh tokens
  await admin.auth().revokeRefreshTokens(targetUid);

  // 3. Clean up Firestore user profile & entitlements
  const userRef = db.collection("users").document(targetUid);
  await userRef.collection("entitlements").document("current").delete();
  await userRef.update({
    role: "DELETED",
    isPremium: false,
    name: "Deleted User",
    email: "deleted@jukti.in",
    deletedAt: Date.now()
  });

  // 4. Audit Log
  await db.collection("activity_logs").add({
    actorUid: context.auth!.uid,
    actorRole: "OWNER",
    action: `Permanently deleted user auth and profile for UID: ${targetUid}`,
    targetUid,
    timestamp: admin.firestore.FieldValue.serverTimestamp(),
    result: "SUCCESS"
  });

  return { success: true };
});
