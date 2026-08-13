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
  const userDoc = await db.collection("users").doc(uid).get();
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
  const userDoc = await db.collection("users").doc(uid).get();
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
  const userRef = db.collection("users").doc(sanitizedEmail);
  const entitlementRef = userRef.collection("entitlements").doc("current");
  const historyRef = userRef.collection("entitlement_history").doc(purchaseId);

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
export const processPurchaseRequest = functions.firestore
  .document("users/{uid}/purchaseRequests/{requestId}")
  .onCreate(async (snap, context) => {
    const data = snap.data();
    const { uid, requestId } = context.params;

    if (data.status !== "PENDING_VERIFICATION") {
      functions.logger.info(`Request ${requestId} is not PENDING_VERIFICATION, skipping.`);
      return;
    }

    const { purchaseToken, productId, packageName } = data;
    
    if (!purchaseToken || !productId || !packageName) {
      functions.logger.error(`Request ${requestId} missing required fields.`);
      await snap.ref.update({ status: "FAILED", error: "Missing verification parameters" });
      return;
    }

    // Security: Only allow the configured production package name
    const EXPECTED_PACKAGE_NAME = "com.aistudio.jukti.examprep.app";
    if (packageName !== EXPECTED_PACKAGE_NAME) {
      functions.logger.error(`Request ${requestId} has invalid package name: ${packageName}`);
      await snap.ref.update({ status: "FAILED", error: "PACKAGE_MISMATCH" });
      return;
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
      // 1. Query the Play Console for in-app product details
      // Assuming Jukti uses one-time in-app products for plans. If using subscriptions, this would need purchases.subscriptionsv2.get
      const response = await playDeveloperApi.purchases.products.get({
        packageName,
        productId,
        token: purchaseToken
      });

      const purchaseState = response.data.purchaseState; // 0 = purchased, 1 = canceled, 2 = pending
      if (purchaseState !== 0) {
        functions.logger.warn(`Purchase ${requestId} invalid state: ${purchaseState}`);
        await snap.ref.update({ status: "FAILED", error: "NOT_PURCHASED" });
        return;
      }

      // 2. Replay Protection & Idempotency check via Firestore transaction
      const purchaseId = "play_" + purchaseToken.substring(0, 20);
      const purchaseRef = db.collection("users").doc(uid).collection("purchases").doc(purchaseId);
      
      // We will create individual entitlements per plan as requested by rules
      const entitlementRef = db.collection("users").doc(uid).collection("entitlements").doc(productId);
      const historyRef = db.collection("users").doc(uid).collection("entitlement_history").doc(purchaseId);

      await db.runTransaction(async (transaction) => {
        const purchaseDoc = await transaction.get(purchaseRef);
        if (purchaseDoc.exists && (purchaseDoc.data()?.status === "COMPLETED" || purchaseDoc.data()?.status === "PROVISIONED")) {
          // Already processed, just update the request status
          transaction.update(snap.ref, { status: "VERIFIED_ALREADY_PROCESSED" });
          return;
        }

        const now = Date.now();
        // Acknowledge the purchase if needed
        if (response.data.acknowledgementState === 0) { // 0 = Yet to be acknowledged, 1 = Acknowledged
          try {
             await playDeveloperApi.purchases.products.acknowledge({
                packageName,
                productId,
                token: purchaseToken,
                requestBody: { developerPayload: "acknowledged_by_server" }
             });
          } catch (ackError) {
             functions.logger.error("Failed to acknowledge purchase, proceeding anyway but could cause refund", ackError);
             // We continue provisioning even if ack fails, though in production you might want to retry or handle it.
          }
        }

        // Provision authoritative entitlement
        // Determine validity on server. Assuming 1 year if not specified by a backend configuration.
        // For one-time products, Google Play doesn't return expiry, so we set it.
        const durationMs = 365 * 24 * 60 * 60 * 1000; 

        const entitlementMap = {
          planId: productId,
          planName: data.planName || "Play subscription/product",
          status: "ACTIVE",
          validFrom: now,
          validUntil: now + durationMs,
          benefits: "premium_content,ad_free,mock_tests",
          source: "GOOGLE_PLAY",
          purchaseId: purchaseId,
          updatedAt: now
        };

        const purchaseMap = {
          purchaseId,
          purchaseToken,
          productId,
          packageName,
          timestamp: now,
          status: "COMPLETED",
          verificationStatus: "VERIFIED"
        };

        transaction.set(purchaseRef, purchaseMap);
        transaction.set(entitlementRef, entitlementMap);
        transaction.set(historyRef, entitlementMap);
        
        // Optionally keep "current" for backward compatibility, but we now rely on individual entitlements
        transaction.set(db.collection("users").doc(uid).collection("entitlements").doc("current"), entitlementMap);
        
        transaction.update(db.collection("users").doc(uid), { isPremium: true });
        
        // Update request document
        transaction.update(snap.ref, { 
          status: "VERIFIED", 
          resolvedAt: now,
          purchaseId: purchaseId
        });
      });

      functions.logger.info(`Successfully verified and provisioned purchase ${purchaseId} for user ${uid}`);

    } catch (err: any) {
      functions.logger.error("Play purchase verification failed", err);
      // Determine if error is a 400 (invalid token, mismatch) or 500 (API down)
      const isGoogleApiError = err.response && err.response.status;
      const errorCode = isGoogleApiError ? `GOOGLE_API_${err.response.status}` : "VERIFICATION_ERROR";
      await snap.ref.update({ status: "FAILED", error: errorCode, errorDetails: err.message });
    }
  });

// 4. Atomic Pending Request Approval
export const approvePendingRequest = functions.https.onCall(async (data, context) => {
  await requireAdminOrOwner(context);

  const { requestId } = data;
  if (!requestId) {
    throw new functions.https.HttpsError("invalid-argument", "Missing request ID");
  }

  const reqRef = db.collection("pending_requests").doc(requestId);

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
  await db.collection("users").doc(targetUid).update({
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
  const userRef = db.collection("users").doc(targetUid);
  await userRef.collection("entitlements").doc("current").delete();
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

// 7. Google Play RTDN (Real-Time Developer Notifications) for Refunds/Revocations
export const playBillingRtdn = functions.pubsub.topic('play_billing').onPublish(async (message) => {
  try {
    const dataString = Buffer.from(message.data, 'base64').toString('utf8');
    const rtdnData = JSON.parse(dataString);
    
    if (rtdnData.subscriptionNotification) {
        // Subscription handling if app supports subscriptions in future
        functions.logger.info("Received subscription notification", rtdnData.subscriptionNotification);
        return;
    }

    if (rtdnData.oneTimeProductNotification) {
      const notification = rtdnData.oneTimeProductNotification;
      const notificationType = notification.notificationType;
      // 2 = CANCELED, 3 = PURCHASED (already handled by app), 
      // 1 = REVOKED (For some subscription cases or specific refunds)
      // For OneTimeProductNotification, there is no standardized list of notification types in some older docs,
      // but typically refunds trigger a cancellation.
      
      const purchaseToken = notification.purchaseToken;
      const purchaseId = "play_" + purchaseToken.substring(0, 20);

      if (notificationType === 2 || notificationType === 1) { // Cancelled or Revoked
        functions.logger.warn(`Purchase revoked or canceled via RTDN: ${purchaseId}`);
        
        // Find the purchase in Firestore (Need to query across all users or use a global purchases collection)
        // Since we store purchases under users/{uid}/purchases/{purchaseId}, we need a collectionGroup query
        const purchaseQuery = await db.collectionGroup("purchases").where("purchaseId", "==", purchaseId).limit(1).get();
        if (purchaseQuery.empty) {
          functions.logger.error(`Could not find purchase ${purchaseId} for revocation`);
          return;
        }

        const purchaseDoc = purchaseQuery.docs[0];
        const userRef = purchaseDoc.ref.parent.parent;
        if (!userRef) return;
        const uid = userRef.id;
        const productId = purchaseDoc.data().productId;

        await db.runTransaction(async (transaction) => {
          transaction.update(purchaseDoc.ref, { status: "REVOKED", updatedAt: Date.now() });
          
          const entitlementRef = userRef.collection("entitlements").doc(productId);
          transaction.update(entitlementRef, { status: "REVOKED", validUntil: Date.now(), updatedAt: Date.now() });
          
          const historyRef = userRef.collection("entitlement_history").doc(purchaseId);
          transaction.update(historyRef, { status: "REVOKED", updatedAt: Date.now() });
          
          // Also update current if it matches
          const currentRef = userRef.collection("entitlements").doc("current");
          const currentDoc = await transaction.get(currentRef);
          if (currentDoc.exists && currentDoc.data()?.purchaseId === purchaseId) {
             transaction.update(currentRef, { status: "REVOKED", validUntil: Date.now(), updatedAt: Date.now() });
             transaction.update(userRef, { isPremium: false }); // Revoke premium flag
          }
        });

        functions.logger.info(`Successfully revoked entitlement for user ${uid} purchase ${purchaseId}`);
      }
    }
  } catch (e) {
    functions.logger.error("Failed to process RTDN message", e);
  }
});
