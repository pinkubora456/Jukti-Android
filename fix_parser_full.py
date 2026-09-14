import re

path = "app/src/main/java/com/example/util/CsvQuestionParser.kt"
with open(path, "r") as f:
    content = f.read()

# I will find "object CsvQuestionParser {" and keep it, but I will replace the validateAndParseQuestions method.
# Wait, let's just find everything from "fun validateAndParseQuestions(" to the end of the file, and replace it.

start_index = content.find("fun validateAndParseQuestions(")
if start_index == -1:
    print("Could not find validateAndParseQuestions")
else:
    # First, let's check if there are multiple occurrences.
    parts = content.split("fun validateAndParseQuestions(")
    
    # Just take everything before the FIRST occurrence
    prefix = parts[0]
    
    new_func = """fun validateAndParseQuestions(
        csvText: String,
        defaultSubject: String = "General Studies",
        defaultChapter: String = "General",
        defaultExamCategory: String = "",
        isPremium: Boolean = false,
        existingQuestions: List<QuestionEntity> = emptyList(),
        contentType: String = "Normal MCQ"
    ): BatchValidationResult {
        val parsedRows = parseCsv(csvText)
        if (parsedRows.isEmpty()) {
            return BatchValidationResult(
                totalRows = 0,
                validRows = emptyList(),
                invalidRows = emptyList(),
                duplicateInQBankRows = emptyList(),
                duplicateInBatchRows = emptyList(),
                passages = emptyList()
            )
        }

        val hasHeader = isHeaderRow(parsedRows[0])
        val dataRows = if (hasHeader) parsedRows.drop(1) else parsedRows
        val startIndex = if (hasHeader) 2 else 1

        val headerMap: Map<String, Int> = if (hasHeader) {
            parsedRows[0].mapIndexed { index, col ->
                col.trim().trim('\"').trim().lowercase().replace(" ", "").replace("_", "") to index
            }.toMap()
        } else emptyMap()

        fun findHeaderCol(vararg keys: String): Int? {
            for (k in keys) {
                val normalizedKey = k.trim().lowercase().replace(" ", "").replace("_", "")
                val idx = headerMap[normalizedKey]
                if (idx != null) return idx
            }
            return null
        }

        val colQEn = findHeaderCol("statement", "statementenglish", "statementen", "questioninenglish", "questionenglish", "question", "questionen")
        val colQAs = findHeaderCol("statementassamese", "statementas", "questioninassamese", "questionassamese", "questionas")
        val colOpAEn = findHeaderCol("a", "optiona", "optionaenglish", "optionaen")
        val colOpAAs = findHeaderCol("aas", "aassamese", "optionaassamese", "optionaas")
        val colOpBEn = findHeaderCol("b", "optionb", "optionbenglish", "optionben")
        val colOpBAs = findHeaderCol("bas", "bassamese", "optionbassamese", "optionbas")
        val colOpCEn = findHeaderCol("c", "optionc", "optioncenglish", "optioncen")
        val colOpCAs = findHeaderCol("cas", "cassamese", "optioncassamese", "optioncas")
        val colOpDEn = findHeaderCol("d", "optiond", "optiondenglish", "optionden")
        val colOpDAs = findHeaderCol("das", "dassamese", "optiondassamese", "optiondas")
        val colCorrect = findHeaderCol("correctanswer", "correct", "answer", "ans", "key")
        val colExpEn = findHeaderCol("explanation", "explanationenglish", "explanationen")
        val colExpAs = findHeaderCol("explanationassamese", "explanationas")
        val colSubj = findHeaderCol("subject", "subj")
        val colTopic = findHeaderCol("topic", "chapter", "unit")
        val colTags = findHeaderCol("tags", "tag", "type", "category", "questiontype")
        val colDiff = findHeaderCol("difficulty", "diff", "level")
        val colTargetExams = findHeaderCol("targetexams", "targetexam", "examcategory", "exam")
        val colQuestionFor = findHeaderCol("questionfor", "accesstype", "ispremium", "access")
        val colPassageId = findHeaderCol("passageid", "passage_id")
        val colPassage = findHeaderCol("passage", "passage_text")

        val validList = mutableListOf<ParsedQuestionRow>()
        val invalidList = mutableListOf<ParsedQuestionRow>()
        val duplicateInQBankList = mutableListOf<ParsedQuestionRow>()
        val duplicateInBatchList = mutableListOf<ParsedQuestionRow>()
        
        val passagesList = mutableListOf<com.example.data.local.ReadingComprehensionPassageEntity>()
        val seenPassages = mutableSetOf<String>()

        val seenQuestionsInBatch = mutableMapOf<String, Int>()
        
        val existingKeys = mutableMapOf<String, Long>()
        for (q in existingQuestions) {
            val key = if (q.duplicateKey.isNotBlank()) q.duplicateKey else generateDuplicateKey(q.questionEn)
            if (key.isNotBlank()) {
                existingKeys[key] = q.id
            }
        }

        dataRows.forEachIndexed { index, row ->
            val rowNum = startIndex + index
            val rawPreview = row.joinToString(" | ").take(100)
            val errors = mutableListOf<String>()

            if (row.isEmpty() || row.all { it.isBlank() }) {
                invalidList.add(ParsedQuestionRow(rowNumber = rowNum, question = null, isValid = false, errorReasons = listOf("Empty row"), rawPreview = rawPreview))
                return@forEachIndexed
            }

            try {
                var qEn = ""
                var qAs = ""
                var opAEn = ""
                var opAAs = ""
                var opBEn = ""
                var opBAs = ""
                var opCEn = ""
                var opCAs = ""
                var opDEn = ""
                var opDAs = ""
                var correctRaw = ""
                var expEn = ""
                var expAs = ""
                var subj = defaultSubject
                var topic = defaultChapter
                var tags = "Expected"
                var diff = "Medium"
                var targetExams = defaultExamCategory
                var parsedIsPremium = isPremium
                var passageId = ""
                var passage = ""

                if (hasHeader) {
                    if (colQEn != null && colQEn < row.size) qEn = row[colQEn]
                    if (colQAs != null && colQAs < row.size) qAs = row[colQAs]
                    if (colOpAEn != null && colOpAEn < row.size) opAEn = row[colOpAEn]
                    if (colOpAAs != null && colOpAAs < row.size) opAAs = row[colOpAAs]
                    if (colOpBEn != null && colOpBEn < row.size) opBEn = row[colOpBEn]
                    if (colOpBAs != null && colOpBAs < row.size) opBAs = row[colOpBAs]
                    if (colOpCEn != null && colOpCEn < row.size) opCEn = row[colOpCEn]
                    if (colOpCAs != null && colOpCAs < row.size) opCAs = row[colOpCAs]
                    if (colOpDEn != null && colOpDEn < row.size) opDEn = row[colOpDEn]
                    if (colOpDAs != null && colOpDAs < row.size) opDAs = row[colOpDAs]
                    if (colCorrect != null && colCorrect < row.size) correctRaw = row[colCorrect]
                    if (colExpEn != null && colExpEn < row.size) expEn = row[colExpEn]
                    if (colExpAs != null && colExpAs < row.size) expAs = row[colExpAs]
                    if (colSubj != null && colSubj < row.size && row[colSubj].isNotBlank()) subj = row[colSubj]
                    if (colTopic != null && colTopic < row.size && row[colTopic].isNotBlank()) topic = row[colTopic]
                    if (colTags != null && colTags < row.size && row[colTags].isNotBlank()) tags = row[colTags]
                    if (colDiff != null && colDiff < row.size && row[colDiff].isNotBlank()) diff = row[colDiff]
                    if (colTargetExams != null && colTargetExams < row.size && row[colTargetExams].isNotBlank()) targetExams = row[colTargetExams]
                    if (colQuestionFor != null && colQuestionFor < row.size && row[colQuestionFor].isNotBlank()) {
                        val qFor = row[colQuestionFor].trim().lowercase()
                        if (qFor == "premium" || qFor == "true" || qFor == "yes" || qFor == "paid") {
                            parsedIsPremium = true
                        } else if (qFor == "free" || qFor == "false" || qFor == "no") {
                            parsedIsPremium = false
                        }
                    }
                    if (colPassageId != null && colPassageId < row.size) passageId = row[colPassageId]
                    if (colPassage != null && colPassage < row.size) passage = row[colPassage]
                } else {
                    val size = row.size
                    if (contentType == "Reading Comprehension") {
                        if (size > 0) passageId = row[0]
                        if (size > 1) passage = row[1]
                        if (size > 2) qEn = row[2]
                        if (size > 3) opAEn = row[3]
                        if (size > 4) opBEn = row[4]
                        if (size > 5) opCEn = row[5]
                        if (size > 6) opDEn = row[6]
                        if (size > 7) correctRaw = row[7]
                        if (size > 8) expEn = row[8]
                        if (size > 9) expAs = row[9]
                        if (size > 10) subj = row[10]
                        if (size > 11) topic = row[11]
                        if (size > 12) tags = row[12]
                        if (size > 13) diff = row[13]
                    } else {
                        if (size > 0) qEn = row[0]
                        if (size > 1) qAs = row[1]
                        if (size > 2) opAEn = row[2]
                        if (size > 3) opAAs = row[3]
                        if (size > 4) opBEn = row[4]
                        if (size > 5) opBAs = row[5]
                        if (size > 6) opCEn = row[6]
                        if (size > 7) opCAs = row[7]
                        if (size > 8) opDEn = row[8]
                        if (size > 9) opDAs = row[9]
                        if (size > 10) correctRaw = row[10]
                        if (size > 11) expEn = row[11]
                        if (size > 12) expAs = row[12]
                        if (size > 13) subj = row[13]
                        if (size > 14) topic = row[14]
                        if (size > 15) tags = row[15]
                        if (size > 16) diff = row[16]
                    }
                }

                qEn = qEn.trim()
                qAs = qAs.trim()
                opAEn = opAEn.trim()
                opAAs = opAAs.trim()
                opBEn = opBEn.trim()
                opBAs = opBAs.trim()
                opCEn = opCEn.trim()
                opCAs = opCAs.trim()
                opDEn = opDEn.trim()
                opDAs = opDAs.trim()
                correctRaw = correctRaw.trim()
                expEn = expEn.trim()
                expAs = expAs.trim()
                passageId = passageId.trim()
                passage = passage.trim()

                if (contentType == "Reading Comprehension") {
                    if (passageId.isBlank()) {
                        errors.add("passageId is required for Reading Comprehension")
                    } else {
                        if (!seenPassages.contains(passageId) && passage.isBlank()) {
                            errors.add("Passage is missing for passageId $passageId.")
                        } else if (passage.isNotBlank()) {
                            val existingPassage = passagesList.find { it.passageId == passageId }
                            if (existingPassage != null && existingPassage.passage != passage) {
                                errors.add("Multiple different passages found for passageId $passageId.")
                            } else if (existingPassage == null) {
                                passagesList.add(com.example.data.local.ReadingComprehensionPassageEntity(
                                    passageId = passageId,
                                    passage = passage,
                                    subject = com.example.data.repository.normalizeSubjectName(subj),
                                    chapter = com.example.data.repository.normalizeChapterName(topic, subj),
                                    topic = topic,
                                    difficulty = diff
                                ))
                                seenPassages.add(passageId)
                            }
                        }
                        
                        if (seenPassages.contains(passageId) && passage.isBlank() && !passagesList.any { it.passageId == passageId }) {
                             errors.add("Question refers to unknown passageId $passageId.")
                        }
                    }
                }

                if (qEn.isBlank() && qAs.isBlank()) {
                    errors.add("Question Statement (English or Assamese) is missing")
                }

                if ((opAEn.isBlank() && opAAs.isBlank()) ||
                    (opBEn.isBlank() && opBAs.isBlank()) ||
                    (opCEn.isBlank() && opCAs.isBlank()) ||
                    (opDEn.isBlank() && opDAs.isBlank())) {
                    errors.add("All 4 options (A, B, C, D) must have either English or Assamese values")
                }

                val correctIndex = parseCorrectOption(correctRaw)
                if (correctIndex == -1) {
                    errors.add("Invalid Correct Answer format. Expected A, B, C, or D. Found: '$correctRaw'")
                }

                if (expEn.isBlank() && expAs.isBlank()) {
                    errors.add("Explanation (English or Assamese) is missing")
                }

                if (subj.isBlank() || topic.isBlank()) {
                    errors.add("Subject or Topic (Chapter) is missing")
                }
                
                val qType = if (tags.contains("PYQ", ignoreCase = true)) "PYQ" else "Expected"
                val pyqExamsValue = if (qType == "PYQ") {
                    val pTags = tags.split(",").map { it.trim() }.filter { !it.equals("PYQ", ignoreCase = true) }
                    pTags.joinToString(", ")
                } else ""
                
                val dupKey = generateDuplicateKey(if (qEn.isNotBlank()) qEn else qAs)
                
                val isComprehension = contentType == "Reading Comprehension"
                val entityType = if (isComprehension) "comprehension" else "normal"

                val qEntity = QuestionEntity(
                    subject = com.example.data.repository.normalizeSubjectName(subj),
                    topic = com.example.data.repository.normalizeChapterName(topic, subj),
                    difficulty = diff.ifBlank { "Medium" },
                    questionEn = qEn,
                    questionAs = qAs,
                    optionAEn = opAEn,
                    optionAAs = opAAs,
                    optionBEn = opBEn,
                    optionBAs = opBAs,
                    optionCEn = opCEn,
                    optionCAs = opCAs,
                    optionDEn = opDEn,
                    optionDAs = opDAs,
                    correctOptionIndex = correctIndex,
                    explanationEn = expEn,
                    explanationAs = expAs,
                    examCategory = targetExams,
                    isPremium = parsedIsPremium,
                    accessType = if (parsedIsPremium) "PREMIUM" else "FREE",
                    questionType = qType,
                    pyqExams = pyqExamsValue,
                    status = "ACTIVE",
                    duplicateKey = dupKey,
                    contentType = entityType,
                    passageId = passageId
                )

                if (errors.isNotEmpty()) {
                    invalidList.add(ParsedQuestionRow(rowNumber = rowNum, question = qEntity, isValid = false, errorReasons = errors, rawPreview = rawPreview))
                } else {
                    var duplicateInBatch = false
                    val existingRowInBatch = seenQuestionsInBatch[dupKey]
                    if (existingRowInBatch != null) {
                        duplicateInBatch = true
                        duplicateInBatchList.add(ParsedQuestionRow(rowNumber = rowNum, question = qEntity, isValid = true, errorReasons = emptyList(), isDuplicateInBatch = true, rawPreview = rawPreview))
                    } else {
                        seenQuestionsInBatch[dupKey] = rowNum
                    }

                    if (!duplicateInBatch) {
                        val existingId = existingKeys[dupKey]
                        if (existingId != null) {
                            duplicateInQBankList.add(ParsedQuestionRow(rowNumber = rowNum, question = qEntity, isValid = true, errorReasons = emptyList(), isExistingInQBank = true, existingQBankId = existingId, rawPreview = rawPreview))
                        } else {
                            validList.add(ParsedQuestionRow(rowNumber = rowNum, question = qEntity, isValid = true, errorReasons = emptyList(), rawPreview = rawPreview))
                        }
                    }
                }
            } catch (e: Exception) {
                invalidList.add(ParsedQuestionRow(rowNumber = rowNum, question = null, isValid = false, errorReasons = listOf("Parsing exception: ${e.message}"), rawPreview = rawPreview))
            }
        }

        return BatchValidationResult(
            totalRows = dataRows.size,
            validRows = validList,
            invalidRows = invalidList,
            duplicateInQBankRows = duplicateInQBankList,
            duplicateInBatchRows = duplicateInBatchList,
            passages = passagesList
        )
    }

    private fun parseCorrectOption(ans: String): Int {
        var trimmed = ans.trim().uppercase()
        trimmed = trimmed.removePrefix("(").removeSuffix(")").removeSuffix(".").removeSuffix(":").trim()
        return when (trimmed) {
            "A", "OPTION A", "OPTIONA", "OPTION 1", "1", "0" -> 0
            "B", "OPTION B", "OPTIONB", "OPTION 2", "2" -> 1
            "C", "OPTION C", "OPTIONC", "OPTION 3", "3" -> 2
            "D", "OPTION D", "OPTIOND", "OPTION 4", "4" -> 3
            else -> -1
        }
    }
}
"""
    
    final_content = prefix + new_func
    
    # We might have duplicates of parseCorrectOption if it was originally there, let's just make sure we only have one closed brace for the object.
    
    with open(path, "w") as f:
        f.write(final_content)
    print("Replaced validateAndParseQuestions")
