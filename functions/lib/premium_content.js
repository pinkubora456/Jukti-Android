"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.getPremiumContent = void 0;
const functions = require("firebase-functions");
const admin = require("firebase-admin");
const db = admin.firestore();
exports.getPremiumContent = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError("unauthenticated", "Authentication required");
    }
    const uid = context.auth.uid;
    // 1. Fetch user's entitlements
    const entitlementsSnap = await db.collection("users").doc(uid).collection("entitlements").where("status", "==", "ACTIVE").get();
    const now = Date.now();
    let hasValidEntitlement = false;
    let hasAllExamsAccess = false;
    let combinedTargetExams = [];
    for (const doc of entitlementsSnap.docs) {
        const ent = doc.data();
        if (ent.validUntil && ent.validUntil > now) {
            hasValidEntitlement = true;
            if (ent.targetExam) {
                if (ent.targetExam.includes("ALL_EXAMS")) {
                    hasAllExamsAccess = true;
                }
                else {
                    const targets = ent.targetExam.split(",").map((s) => s.trim().toLowerCase());
                    combinedTargetExams.push(...targets);
                }
            }
        }
    }
    // Also read plans if entitlement targetExam is not populated
    if (hasValidEntitlement && !hasAllExamsAccess) {
        const plansSnap = await db.collection("plans").get();
        for (const doc of entitlementsSnap.docs) {
            const ent = doc.data();
            if (ent.validUntil && ent.validUntil > now) {
                const matchingPlan = plansSnap.docs.find(p => p.id === ent.planId || p.data().planName === ent.planName);
                if (matchingPlan) {
                    const targetExam = matchingPlan.data().examTarget || "";
                    if (targetExam.toUpperCase().includes("ALL EXAMS") || targetExam.toUpperCase().includes("ALL_EXAMS") || targetExam === "") {
                        hasAllExamsAccess = true;
                    }
                    else {
                        const targets = targetExam.split(",").map((s) => s.trim().toLowerCase());
                        combinedTargetExams.push(...targets);
                    }
                }
            }
        }
    }
    if (!hasValidEntitlement) {
        return { questions: [], mockTests: [], studyNotes: [] }; // No premium access
    }
    // Fetch Premium Content
    const qs = await db.collection("questions").where("isPremium", "==", true).get();
    const ms = await db.collection("mock_tests").where("isPremium", "==", true).get();
    const sn = await db.collection("study_notes").where("isPremium", "==", true).get();
    function matchesTarget(itemCategory, itemSubject, targets) {
        if (hasAllExamsAccess)
            return true;
        const combined = (itemCategory + " " + itemSubject).toLowerCase();
        return targets.some(target => combined.includes(target));
    }
    return {
        questions: qs.docs.map(d => d.data()).filter(d => matchesTarget(d.examCategory || "", (d.subject || "") + " " + (d.topic || ""), combinedTargetExams)),
        mockTests: ms.docs.map(d => d.data()).filter(d => matchesTarget(d.category || "", d.subjectOrChapter || "", combinedTargetExams)),
        studyNotes: sn.docs.map(d => d.data()).filter(d => matchesTarget(d.subject || "", d.topic || "", combinedTargetExams))
    };
});
//# sourceMappingURL=premium_content.js.map