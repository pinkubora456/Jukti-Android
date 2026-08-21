const fs = require('fs');
let code = fs.readFileSync('functions/src/index.ts', 'utf8');

const regex = /const currentDoc = await transaction\.get\(currentRef\);/;
const replacement = `
        if (productId === "STARTER_7_DAY") {
            const starterHistoryRef = db.collection("users").doc(uid).collection("entitlement_history").doc("STARTER_7_DAY");
            const starterHistoryDoc = await transaction.get(starterHistoryRef);
            if (starterHistoryDoc.exists) {
                 functions.logger.warn("Starter Pass already used by user", uid);
                 transaction.update(snap.ref, { status: "FAILED", error: "STARTER_ALREADY_USED" });
                 return;
            }
            // Mark it in history specifically to prevent ever buying again
            transaction.set(starterHistoryRef, { status: "COMPLETED", timestamp: now, purchaseId });
        }
        
        const currentDoc = await transaction.get(currentRef);`;

code = code.replace(regex, replacement);
fs.writeFileSync('functions/src/index.ts', code);
