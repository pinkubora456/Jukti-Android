with open("functions/src/premium_content.ts", "r") as f:
    text = f.read()

# Replace the fetching logic
new_logic = """
  let hasValidEntitlement = false;
  let hasAllExamsAccess = false;
  let combinedTargetExams: string[] = [];

  for (const doc of entitlementsSnap.docs) {
    const ent = doc.data();
    if (ent.validUntil && ent.validUntil > now) {
      hasValidEntitlement = true;
      if (ent.targetExam) {
          if (ent.targetExam.includes("ALL_EXAMS")) {
              hasAllExamsAccess = true;
          } else {
              const targets = ent.targetExam.split(",").map((s: string) => s.trim().toLowerCase());
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
                  } else {
                      const targets = targetExam.split(",").map((s: string) => s.trim().toLowerCase());
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

  function matchesTarget(itemCategory: string, itemSubject: string, targets: string[]): boolean {
      if (hasAllExamsAccess) return true;
      const combined = (itemCategory + " " + itemSubject).toLowerCase();
      return targets.some(target => combined.includes(target));
  }

  return {
    questions: qs.docs.map(d => d.data()).filter(d => matchesTarget(d.examCategory || "", (d.subject || "") + " " + (d.topic || ""), combinedTargetExams)),
    mockTests: ms.docs.map(d => d.data()).filter(d => matchesTarget(d.category || "", d.subjectOrChapter || "", combinedTargetExams)),
    studyNotes: sn.docs.map(d => d.data()).filter(d => matchesTarget(d.subject || "", d.topic || "", combinedTargetExams))
  };
"""

text = text.replace("""  let hasValidEntitlement = false;
  
  for (const doc of entitlementsSnap.docs) {
    const ent = doc.data();
    if (ent.validUntil && ent.validUntil > now) {
      hasValidEntitlement = true;
      // You could push specific exams based on planId if needed
      // allowedExams.push(ent.targetExam);
    }
  }

  if (!hasValidEntitlement) {
    return { questions: [], mockTests: [], studyNotes: [] }; // No premium access
  }

  // 2. Fetch Premium Content
  // Note: For large collections, returning everything is bad, but Jukti seems to load all questions.
  const qs = await db.collection("questions").where("isPremium", "==", true).get();
  const ms = await db.collection("mock_tests").where("isPremium", "==", true).get();
  const sn = await db.collection("study_notes").where("isPremium", "==", true).get();

  return {
    questions: qs.docs.map(d => d.data()),
    mockTests: ms.docs.map(d => d.data()),
    studyNotes: sn.docs.map(d => d.data())
  };""", new_logic)

with open("functions/src/premium_content.ts", "w") as f:
    f.write(text)
