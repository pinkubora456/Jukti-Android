const fs = require('fs');
let code = fs.readFileSync('functions/src/index.ts', 'utf8');

// The error is TS6133 'purchaseToken' is never read inside verifyAndProvisionStarterPass.
// Let's just remove that line or use it in logger.
code = code.replace(/const \{ purchaseToken, purchaseId, productId \} = data;/, "const { purchaseId, productId } = data;");

fs.writeFileSync('functions/src/index.ts', code);
