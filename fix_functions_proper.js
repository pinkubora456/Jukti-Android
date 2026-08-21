const fs = require('fs');
let code = fs.readFileSync('functions/src/index.ts', 'utf8');

const regex = /\/\/ Provision authoritative entitlement[\s\S]*?const durationMs = 365 \* 24 \* 60 \* 60 \* 1000;\s*const entitlementMap = \{[\s\S]*?updatedAt: now\s*\};/;

const replacement = `
        // Fetch authoritative plan config
        const plansQuery = await db.collection("plans").where("googlePlayProductId", "==", productId).limit(1).get();
        let durationMs = 365 * 24 * 60 * 60 * 1000;
        let authoritativePlanName = data.planName || "Play subscription/product";
        let planInternalId = productId;
        
        if (productId === "STARTER_7_DAY") {
            authoritativePlanName = "Jukti 7-Day Starter Pass";
            planInternalId = "STARTER_7_DAY";
            durationMs = 7 * 24 * 60 * 60 * 1000;
        } else if (!plansQuery.empty) {
            const planData = plansQuery.docs[0].data();
            authoritativePlanName = planData.planName || authoritativePlanName;
            planInternalId = plansQuery.docs[0].id;
            const validityStr = planData.planValidity;
            if (validityStr) {
                const match = validityStr.toLowerCase().trim().match(/^(\\d+)\\s*(day|month|year)s?$/);
                if (match) {
                    const num = parseInt(match[1]);
                    const unit = match[2];
                    if (unit === 'day') durationMs = num * 24 * 60 * 60 * 1000;
                    else if (unit === 'month') durationMs = num * 30 * 24 * 60 * 60 * 1000;
                    else if (unit === 'year') durationMs = num * 365 * 24 * 60 * 60 * 1000;
                }
            }
        } else {
           functions.logger.error("Purchased product ID not found in plans collection: " + productId);
           transaction.update(snap.ref, { status: "FAILED", error: "UNKNOWN_PRODUCT_ID" });
           return;
        }

        // Check existing current entitlement to handle stacking/upgrade safely
        const currentRef = db.collection("users").doc(uid).collection("entitlements").doc("current");
        const currentDoc = await transaction.get(currentRef);
        let newValidUntil = now + durationMs;
        
        if (currentDoc.exists) {
            const currentData = currentDoc.data();
            if (currentData && currentData.status === "ACTIVE" && currentData.validUntil > now) {
                 if (currentData.productId === productId) {
                     // Same plan: stack duration
                     newValidUntil = currentData.validUntil + durationMs;
                 } else {
                     // Different plan: upgrade policy (ensure they don't lose paid time)
                     newValidUntil = Math.max(now + durationMs, currentData.validUntil);
                 }
            }
        }

        const entitlementMap = {
          planId: planInternalId,
          productId: productId,
          planName: authoritativePlanName,
          status: "ACTIVE",
          validFrom: now,
          validUntil: newValidUntil,
          benefits: "premium_content,ad_free,mock_tests",
          source: "GOOGLE_PLAY",
          purchaseId: purchaseId,
          updatedAt: now
        };
`;

code = code.replace(regex, replacement);
fs.writeFileSync('functions/src/index.ts', code);
