package com.example.util

import com.example.data.local.QuestionEntity
import com.example.data.repository.normalizeChapterName
import com.example.data.repository.normalizeSubjectName

data class ParsedQuestionRow(
    val rowNumber: Int,
    val question: QuestionEntity?,
    val isValid: Boolean,
    val errorReasons: List<String>,
    val isDuplicateInBatch: Boolean = false,
    val isExistingInQBank: Boolean = false,
    val existingQBankId: Long? = null,
    val rawPreview: String = ""
)

data class BatchValidationResult(
    val totalRows: Int,
    val validRows: List<ParsedQuestionRow>,
    val invalidRows: List<ParsedQuestionRow>,
    val duplicateInQBankRows: List<ParsedQuestionRow>,
    val duplicateInBatchRows: List<ParsedQuestionRow>
) {
    val validCount: Int get() = validRows.size
    val invalidCount: Int get() = invalidRows.size
    val duplicateCount: Int get() = duplicateInQBankRows.size + duplicateInBatchRows.size
}

object CsvQuestionParser {

    const val SAMPLE_CSV_HEADER = "statement,statementAssamese,a,a_as,b,b_as,c,c_as,d,d_as,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty"

    const val SAMPLE_CSV_ROW_1 = "\"Who was the first King of the Ahom Kingdom?\",\"আহোম ৰাজ্যৰ প্ৰথম ৰজা কোন আছিল?\",\"Sukaphaa\",\"চ্যুকাফা\",\"Sutephaa\",\"চ্যুটেফা\",\"Subinphaa\",\"চুবিনফা\",\"Sudangphaa\",\"চুডাংফা\",\"A\",\"Sukaphaa founded the Ahom Kingdom in medieval Assam.\",\"চ্যুকাফাই মধ্যযুগীয় অসমত আহোম ৰাজ্য প্ৰতিষ্ঠা কৰিছিল।\",\"Assam History\",\"Ahom Kingdom\",\"ADRE HS 2024\",\"Medium\""
    const val SAMPLE_CSV_ROW_2 = "\"Kaziranga National Park is famous for which animal?\",\"কাজিৰঙা ৰাষ্ট্ৰীয় উদ্যান কোনটো প্ৰাণীৰ বাবে বিখ্যাত?\",\"One-horned Rhinoceros\",\"এশিঙীয়া গঁড়\",\"Royal Bengal Tiger\",\"ৰয়েল বেংগল টাইগাৰ\",\"Asian Elephant\",\"এছিয়ান হাতী\",\"Snow Leopard\",\"স্ন' লিপাৰ্ড\",\"A\",\"Kaziranga hosts two-thirds of the world's great one-horned rhinoceroses.\",\"কাজিৰঙাত বিশ্বৰ দুই-তৃতীয়াংশ এশিঙীয়া গঁড় আছে।\",\"Assam Geography\",\"National Parks & Wildlife\",\"ADRE, Assam Police\",\"Easy\""

    fun getSampleCsvTemplate(): String {
        return "$SAMPLE_CSV_HEADER\n$SAMPLE_CSV_ROW_1\n$SAMPLE_CSV_ROW_2"
    }

    /**
     * Sanitizes raw CSV text to fix common paste issues, such as an unmatched
     * leading quote on the header or trailing quotes.
     */
    fun cleanCsvInput(raw: String): String {
        if (raw.isBlank()) return ""
        val lines = raw.lines().map { it.trim() }
        val cleanedLines = lines.map { line ->
            var l = line
            // If the line starts with an unmatched leading quote before the header or row
            if (l.startsWith("\"") && l.contains(",")) {
                val quoteCount = l.count { it == '\"' }
                if (quoteCount % 2 != 0) {
                    l = l.removePrefix("\"")
                }
            }
            // If the line ends with an unmatched trailing quote
            if (l.endsWith("\"") && l.contains(",")) {
                val quoteCount = l.count { it == '\"' }
                if (quoteCount % 2 != 0) {
                    l = l.removeSuffix("\"")
                }
            }
            l
        }
        return cleanedLines.joinToString("\n")
    }

    /**
     * Parses raw CSV text into rows and columns handling quoted fields,
     * multiline strings, escaped quotes (""), and varied line endings.
     */
    fun parseCsv(csvText: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val cleanedText = cleanCsvInput(csvText)
        if (cleanedText.isBlank()) return rows

        var inQuotes = false
        val currentField = StringBuilder()
        val currentRow = mutableListOf<String>()
        var i = 0
        val len = cleanedText.length

        while (i < len) {
            val c = cleanedText[i]

            if (c == '\"') {
                if (inQuotes && i + 1 < len && cleanedText[i + 1] == '\"') {
                    // Escaped double quote
                    currentField.append('\"')
                    i += 2
                    continue
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                currentRow.add(currentField.toString().trim())
                currentField.clear()
            } else if ((c == '\n' || c == '\r') && !inQuotes) {
                if (c == '\r' && i + 1 < len && cleanedText[i + 1] == '\n') {
                    i++ // skip \r of \r\n
                }
                currentRow.add(currentField.toString().trim())
                currentField.clear()
                // Only add if row is not purely empty
                if (currentRow.any { it.isNotEmpty() }) {
                    rows.add(currentRow.toList())
                }
                currentRow.clear()
            } else {
                currentField.append(c)
            }
            i++
        }

        // Add trailing row if present
        if (currentField.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(currentField.toString().trim())
            if (currentRow.any { it.isNotEmpty() }) {
                rows.add(currentRow.toList())
            }
        }

        return rows
    }

    /**
     * Determines whether the first row is a header row.
     */
    private fun isHeaderRow(firstRow: List<String>): Boolean {
        if (firstRow.isEmpty()) return false
        val headerKeywords = listOf(
            "statement", "question", "option", "subject", "topic", "chapter",
            "correct", "correctanswer", "explanation", "difficulty", "tags",
            "a_as", "b_as", "c_as", "d_as", "target", "exam", "premium", "access"
        )
        val joined = firstRow.joinToString(" ").lowercase().replace(" ", "").replace("_", "")
        return headerKeywords.any { joined.contains(it.replace("_", "")) }
    }

    /**
     * Validates and parses questions from CSV text.
     */
    fun validateAndParseQuestions(
        csvText: String,
        defaultSubject: String = "General Studies",
        defaultChapter: String = "General",
        defaultExamCategory: String = "",
        isPremium: Boolean = false,
        existingQuestions: List<QuestionEntity> = emptyList()
    ): BatchValidationResult {
        val parsedRows = parseCsv(csvText)
        if (parsedRows.isEmpty()) {
            return BatchValidationResult(
                totalRows = 0,
                validRows = emptyList(),
                invalidRows = emptyList(),
                duplicateInQBankRows = emptyList(),
                duplicateInBatchRows = emptyList()
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

        val validList = mutableListOf<ParsedQuestionRow>()
        val invalidList = mutableListOf<ParsedQuestionRow>()
        val duplicateInQBankList = mutableListOf<ParsedQuestionRow>()
        val duplicateInBatchList = mutableListOf<ParsedQuestionRow>()

        val seenQuestionsInBatch = mutableMapOf<String, Int>()

        dataRows.forEachIndexed { index, row ->
            val rowNum = startIndex + index
            val rawPreview = row.joinToString(" | ").take(100)
            val errors = mutableListOf<String>()

            if (row.isEmpty() || row.all { it.isBlank() }) {
                invalidList.add(
                    ParsedQuestionRow(
                        rowNumber = rowNum,
                        question = null,
                        isValid = false,
                        errorReasons = listOf("Empty row"),
                        rawPreview = rawPreview
                    )
                )
                return@forEachIndexed
            }

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
            var correctAnsStr = ""
            var expEn = ""
            var expAs = ""
            var subj = ""
            var topic = ""
            var tags = ""
            var diff = "Medium"
            var targetExamsInRow = ""
            var questionForInRow = ""

            // Extract using header mapping if available
            if (hasHeader && colQEn != null && colOpAEn != null && colOpBEn != null) {
                qEn = colQEn.let { if (it < row.size) row[it].trim() else "" }
                qAs = colQAs?.let { if (it < row.size) row[it].trim() else "" } ?: ""
                opAEn = colOpAEn.let { if (it < row.size) row[it].trim() else "" }
                opAAs = colOpAAs?.let { if (it < row.size) row[it].trim() else "" } ?: ""
                opBEn = colOpBEn.let { if (it < row.size) row[it].trim() else "" }
                opBAs = colOpBAs?.let { if (it < row.size) row[it].trim() else "" } ?: ""
                opCEn = colOpCEn?.let { if (it < row.size) row[it].trim() else "" } ?: ""
                opCAs = colOpCAs?.let { if (it < row.size) row[it].trim() else "" } ?: ""
                opDEn = colOpDEn?.let { if (it < row.size) row[it].trim() else "" } ?: ""
                opDAs = colOpDAs?.let { if (it < row.size) row[it].trim() else "" } ?: ""
                correctAnsStr = colCorrect?.let { if (it < row.size) row[it].trim() else "" } ?: ""
                expEn = colExpEn?.let { if (it < row.size) row[it].trim() else "" } ?: ""
                expAs = colExpAs?.let { if (it < row.size) row[it].trim() else "" } ?: ""
                subj = colSubj?.let { if (it < row.size) row[it].trim() else "" } ?: ""
                topic = colTopic?.let { if (it < row.size) row[it].trim() else "" } ?: ""
                tags = colTags?.let { if (it < row.size) row[it].trim() else "" } ?: ""
                diff = colDiff?.let { if (it < row.size) row[it].trim() else "Medium" } ?: "Medium"
                targetExamsInRow = colTargetExams?.let { if (it < row.size) row[it].trim() else "" } ?: ""
                questionForInRow = colQuestionFor?.let { if (it < row.size) row[it].trim() else "" } ?: ""
            } else if (row.size >= 17) {
                // 17-column standard format or extended (statement, statementAssamese, a, a_as, b, b_as, c, c_as, d, d_as, correctAnswer, explanation, explanationAssamese, subject, topic, tags, difficulty, [targetExams], [questionFor])
                qEn = row.getOrElse(0) { "" }.trim()
                qAs = row.getOrElse(1) { "" }.trim()
                opAEn = row.getOrElse(2) { "" }.trim()
                opAAs = row.getOrElse(3) { "" }.trim()
                opBEn = row.getOrElse(4) { "" }.trim()
                opBAs = row.getOrElse(5) { "" }.trim()
                opCEn = row.getOrElse(6) { "" }.trim()
                opCAs = row.getOrElse(7) { "" }.trim()
                opDEn = row.getOrElse(8) { "" }.trim()
                opDAs = row.getOrElse(9) { "" }.trim()
                correctAnsStr = row.getOrElse(10) { "" }.trim()
                expEn = row.getOrElse(11) { "" }.trim()
                expAs = row.getOrElse(12) { "" }.trim()
                subj = row.getOrElse(13) { "" }.trim()
                topic = row.getOrElse(14) { "" }.trim()
                tags = row.getOrElse(15) { "" }.trim()
                diff = row.getOrElse(16) { "Medium" }.trim()
                targetExamsInRow = row.getOrElse(17) { "" }.trim()
                questionForInRow = row.getOrElse(18) { "" }.trim()
            } else if (row.size >= 11) {
                // 11-column simplified format
                qEn = row.getOrElse(0) { "" }.trim()
                opAEn = row.getOrElse(1) { "" }.trim()
                opBEn = row.getOrElse(2) { "" }.trim()
                opCEn = row.getOrElse(3) { "" }.trim()
                opDEn = row.getOrElse(4) { "" }.trim()
                correctAnsStr = row.getOrElse(5) { "" }.trim()
                expEn = row.getOrElse(6) { "" }.trim()
                subj = row.getOrElse(7) { "" }.trim()
                topic = row.getOrElse(8) { "" }.trim()
                tags = row.getOrElse(9) { "" }.trim()
                diff = row.getOrElse(10) { "Medium" }.trim()
            } else if (row.size >= 7) {
                // 7-column minimal format
                qEn = row.getOrElse(0) { "" }.trim()
                opAEn = row.getOrElse(1) { "" }.trim()
                opBEn = row.getOrElse(2) { "" }.trim()
                opCEn = row.getOrElse(3) { "" }.trim()
                opDEn = row.getOrElse(4) { "" }.trim()
                correctAnsStr = row.getOrElse(5) { "" }.trim()
                subj = row.getOrElse(6) { "" }.trim()
            } else {
                errors.add("Insufficient columns (found ${row.size}, expected at least 7 or 17 columns)")
            }

            // Validation rules
            if (qEn.isBlank()) {
                errors.add("Missing Question text / statement in English")
            }
            if (opAEn.isBlank()) {
                errors.add("Missing Option A")
            }
            if (opBEn.isBlank()) {
                errors.add("Missing Option B")
            }

            // Correct answer validation
            val parsedCorrectIndex = parseCorrectOption(correctAnsStr)
            if (parsedCorrectIndex == -1) {
                errors.add("Invalid Correct Answer '$correctAnsStr' (must be A, B, C, or D)")
            }

            // Fallbacks for Subject and Topic
            val finalSubject = if (subj.isNotBlank()) subj.trim() else normalizeSubjectName(defaultSubject.ifBlank { "General Studies" })
            val finalTopic = if (topic.isNotBlank()) topic.trim() else normalizeChapterName(defaultChapter.ifBlank { "General" }, finalSubject)
            val finalDifficulty = when (diff.lowercase()) {
                "easy" -> "Easy"
                "hard" -> "Hard"
                else -> "Medium"
            }
            val finalQuestionType = if (tags.isNotBlank()) tags else "Expected"

            val finalTargetExams = when {
                targetExamsInRow.isNotBlank() -> targetExamsInRow
                defaultExamCategory.isNotBlank() -> defaultExamCategory
                tags.isNotBlank() -> tags
                else -> ""
            }
            val finalIsPremium = if (questionForInRow.isNotBlank()) {
                questionForInRow.equals("Premium", ignoreCase = true) ||
                questionForInRow.equals("PREMIUM", ignoreCase = true) ||
                questionForInRow.equals("true", ignoreCase = true) ||
                questionForInRow.equals("1", ignoreCase = true)
            } else {
                isPremium
            }

            // Duplicate detection
            val duplicateKey = generateDuplicateKey(qEn)
            var isDuplicateInBatch = false
            var isExistingInQBank = false
            var existingQBankId: Long? = null

            if (duplicateKey.isNotEmpty()) {
                if (seenQuestionsInBatch.containsKey(duplicateKey)) {
                    val prevRow = seenQuestionsInBatch[duplicateKey]!!
                    isDuplicateInBatch = true
                    errors.add("Duplicate question in batch (matches Row $prevRow)")
                } else {
                    seenQuestionsInBatch[duplicateKey] = rowNum
                }

                val matchedQ = existingQuestions.firstOrNull { 
                    (it.duplicateKey.isNotBlank() && it.duplicateKey == duplicateKey) ||
                    generateDuplicateKey(it.questionEn) == duplicateKey
                }
                if (matchedQ != null) {
                    isExistingInQBank = true
                    existingQBankId = matchedQ.id
                }
            }

            if (errors.isNotEmpty()) {
                val invalidRow = ParsedQuestionRow(
                    rowNumber = rowNum,
                    question = null,
                    isValid = false,
                    errorReasons = errors,
                    isDuplicateInBatch = isDuplicateInBatch,
                    isExistingInQBank = isExistingInQBank,
                    existingQBankId = existingQBankId,
                    rawPreview = rawPreview
                )
                invalidList.add(invalidRow)
                if (isDuplicateInBatch) {
                    duplicateInBatchList.add(invalidRow)
                }
            } else {
                val entity = QuestionEntity(
                    id = 0L,
                    subject = finalSubject,
                    topic = finalTopic,
                    difficulty = finalDifficulty,
                    questionEn = qEn,
                    questionAs = qAs,
                    optionAEn = opAEn,
                    optionBEn = opBEn,
                    optionCEn = opCEn,
                    optionDEn = opDEn,
                    optionAAs = opAAs,
                    optionBAs = opBAs,
                    optionCAs = opCAs,
                    optionDAs = opDAs,
                    correctOptionIndex = parsedCorrectIndex,
                    explanationEn = expEn,
                    explanationAs = expAs,
                    examCategory = finalTargetExams,
                    isPremium = finalIsPremium,
                    accessType = if (finalIsPremium) "PREMIUM" else "FREE",
                    questionType = finalQuestionType,
                    duplicateKey = duplicateKey
                )

                val validRow = ParsedQuestionRow(
                    rowNumber = rowNum,
                    question = entity,
                    isValid = true,
                    errorReasons = emptyList(),
                    isDuplicateInBatch = false,
                    isExistingInQBank = isExistingInQBank,
                    existingQBankId = existingQBankId,
                    rawPreview = rawPreview
                )
                validList.add(validRow)
                if (isExistingInQBank) {
                    duplicateInQBankList.add(validRow)
                }
            }
        }

        return BatchValidationResult(
            totalRows = dataRows.size,
            validRows = validList,
            invalidRows = invalidList,
            duplicateInQBankRows = duplicateInQBankList,
            duplicateInBatchRows = duplicateInBatchList
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
