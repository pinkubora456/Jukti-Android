const fs = require('fs');
let code = fs.readFileSync('functions/src/index.ts', 'utf8');

const regex = /if \(\!plansQuery\.empty\) \{/;

const replacement = `if (productId === "STARTER_7_DAY") {
            authoritativePlanName = "Jukti 7-Day Starter Pass";
            planInternalId = "STARTER_7_DAY";
            durationMs = 7 * 24 * 60 * 60 * 1000;
        } else if (!plansQuery.empty) {`;

code = code.replace(regex, replacement);
fs.writeFileSync('functions/src/index.ts', code);
