
// 8. Starter Pass Provisioning
export const verifyAndProvisionStarterPass = functions.https.onCall(async (data, context) => {
  if (!context.auth) throw new functions.https.HttpsError("unauthenticated", "Must be logged in");
  const uid = context.auth.uid;
  const { purchaseToken, purchaseId, productId } = data;

  if (productId !== "STARTER_7_DAY") {
    throw new functions.https.HttpsError("invalid-argument", "Invalid product ID");
  }

  const userRef = db.collection("users").doc(uid);
  const historyRef = userRef.collection("entitlement_history").doc("STARTER_7_DAY");

  return await db.runTransaction(async (transaction) => {
    const historyDoc = await transaction.get(historyRef);
    if (historyDoc.exists) {
      throw new functions.https.HttpsError("already-exists", "Starter Pass already used");
    }

    const now = Date.now();
    const expiry = now + (7 * 24 * 60 * 60 * 1000); // 7 days

    const entitlementMap = {
      planId: "STARTER_7_DAY",
      planName: "Jukti 7-Day Starter Pass",
      status: "ACTIVE",
      validFrom: now,
      validUntil: expiry,
      benefits: "premium_content,ad_free,mock_tests",
      source: "PLAY_BILLING",
      purchaseId: purchaseId,
      updatedAt: now
    };

    transaction.set(userRef.collection("entitlements").doc("STARTER_7_DAY"), entitlementMap);
    transaction.set(historyRef, { status: "COMPLETED", timestamp: now, purchaseId });
    transaction.update(userRef, { isPremium: true });

    return { success: true, expiry };
  });
});
