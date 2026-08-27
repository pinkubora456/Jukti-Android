import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

const db = admin.firestore();

export const getPremiumContent = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Authentication required");
  }
  
  const uid = context.auth.uid;
  const authEmail = (context.auth.token.email || "").toLowerCase().trim();

  // 1. Check user role and Admin/Owner status
  let isAdminOrOwner = false;
  
  // Check custom claims
  if (context.auth.token.admin === true || context.auth.token.owner === true) {
    isAdminOrOwner = true;
  }

  // Check Firestore user profile role
  let userDoc = await db.collection("users").doc(uid).get();
  if (!userDoc.exists && authEmail) {
    // Fallback lookup by email-sanitized doc ID
    const sanitizedEmail = authEmail.replace("@", "_at_").replace(".", "_dot_");
    userDoc = await db.collection("users").doc(sanitizedEmail).get();
  }

  if (userDoc.exists) {
    const userData = userDoc.data() || {};
    const role = (userData.role || "").toUpperCase();
    if (role === "ADMIN" || role === "OWNER") {
      isAdminOrOwner = true;
    }
  }

  const now = Date.now();
  let hasValidEntitlement = false;
  let hasAllExamsAccess = false;
  const combinedTargetExams: string[] = [];

  if (isAdminOrOwner) {
    hasValidEntitlement = true;
    hasAllExamsAccess = true;
  } else {
    // 2. Fetch user's active entitlements
    let entitlementsSnap = await db.collection("users").doc(uid).collection("entitlements").get();
    if (entitlementsSnap.empty && authEmail) {
      const sanitizedEmail = authEmail.replace("@", "_at_").replace(".", "_dot_");
      entitlementsSnap = await db.collection("users").doc(sanitizedEmail).collection("entitlements").get();
    }

    const plansSnap = await db.collection("plans").get();
    const plansDocs = plansSnap.docs.map(d => ({ id: d.id, ...d.data() }));

    for (const doc of entitlementsSnap.docs) {
      const ent = doc.data();
      const status = (ent.status || "").toUpperCase();
      
      // Filter out inactive/free entitlements
      if (status !== "ACTIVE" && status !== "LIFETIME") continue;
      if (ent.planName === "Free Plan" || ent.planId === "free_plan") continue;

      const isLifetime = ent.isLifetime === true || (ent.validityType && ent.validityType.toUpperCase() === "LIFETIME") || status === "LIFETIME";
      const validUntil = ent.validUntil || 0;

      if (isLifetime || (validUntil > 0 && validUntil > now)) {
        hasValidEntitlement = true;

        // Extract targets from entitlement or matching plan
        const matchingPlan: any = plansDocs.find(p => p.id === ent.planId || (p.planName && ent.planName && p.planName.toLowerCase() === ent.planName.toLowerCase()));

        const targetExamStr = ent.targetExam || ent.examTarget || (matchingPlan ? (matchingPlan.examTarget || matchingPlan.targetExam || "") : "");
        const planNameStr = ent.planName || (matchingPlan ? matchingPlan.planName : "");

        const combinedText = (targetExamStr + " " + planNameStr).toLowerCase();

        if (combinedText.includes("all exam") || combinedText.includes("all_exams") || combinedText.includes("combo") || combinedText.includes("mega") || targetExamStr.toUpperCase().includes("ALL EXAMS")) {
          hasAllExamsAccess = true;
        } else {
          if (targetExamStr && targetExamStr.trim().length > 0) {
            const rawParts = targetExamStr.split(/[,|]/).map((s: string) => s.trim()).filter((s: string) => s.length > 0);
            for (const part of rawParts) {
              const pLower = part.toLowerCase();
              if (pLower === "all" || pLower === "all exams" || pLower === "all_exams") {
                hasAllExamsAccess = true;
              } else {
                combinedTargetExams.push(part);
              }
            }
          }
          if (planNameStr) {
            combinedTargetExams.push(planNameStr);
          }
        }
      }
    }
  }

  if (!hasValidEntitlement) {
    return { questions: [], mockTests: [], studyNotes: [] }; // No premium access
  }

  // Fetch Premium Content from Firestore using Admin SDK
  const qs = await db.collection("questions").where("isPremium", "==", true).get();
  const ms = await db.collection("mock_tests").where("isPremium", "==", true).get();
  const sn = await db.collection("study_notes").where("isPremium", "==", true).get();

  function matchesTarget(itemCategory: string, itemSubject: string, itemTopic: string, targets: string[], allAccess: boolean): boolean {
    if (allAccess) return true;
    if (targets.length === 0) return true;

    const cat = (itemCategory || "").trim();
    const sub = (itemSubject || "").trim();
    const top = (itemTopic || "").trim();
    const combined = `${cat} ${sub} ${top}`.toLowerCase();

    return targets.some(rawTarget => {
      const target = rawTarget.toLowerCase().trim();
      if (!target || target === "all" || target === "all exams" || target === "all_exams") return true;

      const isGrade4Plan = target.includes("grade 4") || target.includes("grade iv") || target.includes("class 4");
      const isGrade3Plan = target.includes("grade 3") || target.includes("grade iii") || target.includes("class 3");
      const isDriverPlan = target.includes("driver");
      const isPolicePlan = target.includes("police") || target.includes("constable") || target.includes("si");
      const isTetPlan = target.includes("tet");
      const isApscPlan = target.includes("apsc");

      const isGrade4Item = combined.includes("grade 4") || combined.includes("grade iv") || combined.includes("class 4");
      const isGrade3Item = combined.includes("grade 3") || combined.includes("grade iii") || combined.includes("class 3");
      const isDriverItem = combined.includes("driver");
      const isPoliceItem = combined.includes("police") || combined.includes("constable") || combined.includes("si");
      const isTetItem = combined.includes("tet");
      const isApscItem = combined.includes("apsc");

      if (isGrade4Plan) {
        return isGrade4Item || (!isGrade3Item && combined.includes("adre"));
      }
      if (isGrade3Plan) {
        return isGrade3Item || (!isGrade4Item && combined.includes("adre"));
      }
      if (isDriverPlan) return isDriverItem;
      if (isPolicePlan) return isPoliceItem;
      if (isTetPlan) return isTetItem;
      if (isApscPlan) return isApscItem;

      return combined.includes(target) || cat.toLowerCase().includes(target);
    });
  }

  return {
    questions: qs.docs.map(d => d.data()).filter(d => matchesTarget(d.examCategory || "", d.subject || "", d.topic || "", combinedTargetExams, hasAllExamsAccess)),
    mockTests: ms.docs.map(d => d.data()).filter(d => matchesTarget(d.category || "", d.subjectOrChapter || "", d.titleEn || "", combinedTargetExams, hasAllExamsAccess)),
    studyNotes: sn.docs.map(d => d.data()).filter(d => matchesTarget(d.subject || "", d.topic || "", d.titleEn || "", combinedTargetExams, hasAllExamsAccess))
  };
});
