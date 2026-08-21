const fs = require('fs');
let code = fs.readFileSync('functions/src/index.ts', 'utf8');

// 1. Remove the bad block from playBillingRtdn
const badRegex = /\s*if \(productId === "STARTER_7_DAY"\) \{[\s\S]*?timestamp: now, purchaseId \}\);\s*\}/;
code = code.replace(badRegex, "");

// 2. Insert properly into processPurchaseRequest
const goodRegex = /(const currentRef = db\.collection\("users"\)\.doc\(uid\)\.collection\("entitlements"\)\.doc\("current"\);\s*const currentDoc = await transaction\.get\(currentRef\);)/;

const goodReplacement = `
        if (productId === "STARTER_7_DAY") {
            const starterHistoryRef = db.collection("users").doc(uid).collection("entitlement_history").doc("STARTER_7_DAY");
            const starterHistoryDoc = await transaction.get(starterHistoryRef);
            if (starterHistoryDoc.exists) {
                 functions.logger.warn("Starter Pass already used by user", uid);
                 transaction.update(snap.ref, { status: "FAILED", error: "STARTER_ALREADY_USED" });
                 return;
            }
            transaction.set(starterHistoryRef, { status: "COMPLETED", timestamp: now, purchaseId });
        }
        $1
`;

code = code.replace(goodRegex, goodReplacement);

fs.writeFileSync('functions/src/index.ts', code);
