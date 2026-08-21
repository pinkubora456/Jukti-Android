const fs = require('fs');
let code = fs.readFileSync('functions/src/index.ts', 'utf8');

const regex = /if \(\!plansQuery\.empty\) \{[\s\S]*?\} else \{\n\s*functions\.logger\.warn\("Purchased product ID not found in plans collection:", productId\);\n\s*\}/;

const replacement = `if (!plansQuery.empty) {
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
        }`;

code = code.replace(regex, replacement);
fs.writeFileSync('functions/src/index.ts', code);
