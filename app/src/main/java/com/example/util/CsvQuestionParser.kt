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

    const val SAMPLE_CSV_HEADER = "Question in English,Question in Assamese,a,a_as,b,b_as,c,c_as,d,d_as,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty"

    const val SAMPLE_CSV_ROW_1 = "\"Who was the first King of the Ahom Kingdom?\",\"আহোম ৰাজ্যৰ প্ৰথম ৰজা কোন আছিল?\",\"Sukaphaa\",\"চ্যুকাফা\",\"Sutephaa\",\"চ্যুটেফা\",\"Subinphaa\",\"চুবিনফা\",\"Sudangphaa\",\"চুডাংফা\",\"A\",\"Sukaphaa founded the Ahom Kingdom in 1228.\",\"চ্যুকাফাই ১২২৮ চনত আহোম ৰাজ্য প্ৰতিষ্ঠা কৰিছিল।\",\"Assam History\",\"Ahom Dynasty\",\"Expected\",\"Medium\""
    const val SAMPLE_CSV_ROW_2 = "\"Kaziranga National Park is famous for which animal?\",\"কাজিৰঙা ৰাষ্ট্ৰীয় উদ্যান কোনটো প্ৰাণীৰ বাবে বিখ্যাত?\",\"One-horned Rhinoceros\",\"এশিঙীয়া গঁড়\",\"Royal Bengal Tiger\",\"ৰয়েল বেংগল টাইগাৰ\",\"Asian Elephant\",\"এছিয়ান হাতী\",\"Snow Leopard\",\"স্ন' লিপাৰ্ড\",\"A\",\"Kaziranga hosts two-thirds of the world's great one-horned rhinoceroses.\",\"কাজিৰঙাত বিশ্বৰ দুই-তৃতীয়াংশ এশিঙীয়া গঁড় আছে।\",\"Assam Geography\",\"National Parks & Wildlife\",\"General Exam\",\"Easy\""

    fun getSampleCsvTemplate(): String {
        return "$SAMPLE_CSV_HEADER\n$SAMPLE_CSV_ROW_1\n$SAMPLE_CSV_ROW_2"
    }

    /**
     * Parses raw CSV text into rows and columns handling quoted fields,
     * multiline strings, escaped quotes (""), and varied line endings.
     */
    fun parseCsv(csvText: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        if (csvText.isBlank()) return rows

        var inQuotes = false
        val currentField = StringBuilder()
        val currentRow = mutableListOf<String>()
        var i = 0
        val len = csvText.length

        while (i < len) {
            val c = csvText[i]

            if (c == '\"') {
                if (inQuotes && i + 1 < len && csvText[i + 1] == '\"') {
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
                if (c == '\r' && i + 1 < len && csvText[i + 1] == '\n') {
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
        val headerKeywords = listOf("question", "option", "subject", "topic", "chapter", "correct", "explanation", "difficulty", "tags", "a_as", "b_as")
        val joined = firstRow.joinToString(" ").lowercase()
        return headerKeywords.any { joined.contains(it) }
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

            if (row.size >= 17) {
                // 17-column standard format
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
                errors.add("Missing Question text in English")
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
            val finalSubject = if (subj.isNotBlank()) normalizeSubjectName(subj) else normalizeSubjectName(defaultSubject.ifBlank { "General Studies" })
            val finalTopic = if (topic.isNotBlank()) normalizeChapterName(topic, finalSubject) else normalizeChapterName(defaultChapter.ifBlank { "General" }, finalSubject)
            val finalDifficulty = when (diff.lowercase()) {
                "easy" -> "Easy"
                "hard" -> "Hard"
                else -> "Medium"
            }
            val finalQuestionType = if (tags.isNotBlank()) tags else "Expected"

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
                    it.duplicateKey == duplicateKey
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
                    examCategory = defaultExamCategory,
                    isPremium = isPremium,
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
        val trimmed = ans.trim().uppercase()
        return when (trimmed) {
            "A", "OPTION A", "OPTIONA", "1", "0" -> 0
            "B", "OPTION B", "OPTIONB", "2" -> 1
            "C", "OPTION C", "OPTIONC", "3" -> 2
            "D", "OPTION D", "OPTIOND", "4" -> 3
            else -> -1
        }
    }
}
