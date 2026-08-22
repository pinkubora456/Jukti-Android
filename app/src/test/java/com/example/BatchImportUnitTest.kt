package com.example

import com.example.data.local.QuestionEntity
import com.example.util.CsvQuestionParser
import org.junit.Assert.*
import org.junit.Test

class BatchImportUnitTest {

    @Test
    fun testStandard17ColumnCsvParsing() {
        val sampleCsv = """
            Question in English,Question in Assamese,a,a_as,b,b_as,c,c_as,d,d_as,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty
            "Who founded the Ahom Kingdom?","আহোম ৰাজ্য কোনে প্ৰতিষ্ঠা কৰিছিল?","Sukaphaa","চ্যুকাফা","Sutephaa","চ্যুটেফা","Subinphaa","চুবিনফা","Sudangphaa","চুডাংফা","A","Sukaphaa founded the Ahom Kingdom in 1228.","চ্যুকাফাই ১২২৮ চনত আহোম ৰাজ্য প্ৰতিষ্ঠা কৰিছিল।","Assam History","Ahom Dynasty","Expected","Medium"
            "What is the capital of Assam?","অসমৰ ৰাজধানী কি?","Dispur","দিছপুৰ","Guwahati","গুৱাহাটী","Jorhat","যোৰহাট","Silchar","শিলচৰ","A","Dispur became capital in 1973.","১৯৭৩ চনত দিছপুৰ ৰাজধানী হয়।","Assam Geography","Capital & Districts","PYQ","Easy"
        """.trimIndent()

        val result = CsvQuestionParser.validateAndParseQuestions(
            csvText = sampleCsv,
            defaultSubject = "General Studies",
            defaultChapter = "General",
            defaultExamCategory = "ADRE"
        )

        assertEquals(2, result.totalRows)
        assertEquals(2, result.validCount)
        assertEquals(0, result.invalidCount)

        val q1 = result.validRows[0].question!!
        assertEquals("Who founded the Ahom Kingdom?", q1.questionEn)
        assertEquals("আহোম ৰাজ্য কোনে প্ৰতিষ্ঠা কৰিছিল?", q1.questionAs)
        assertEquals("Sukaphaa", q1.optionAEn)
        assertEquals(0, q1.correctOptionIndex)
        assertEquals("Assam History", q1.subject)
        assertEquals("Ahom Dynasty", q1.topic)
        assertEquals("Medium", q1.difficulty)

        val q2 = result.validRows[1].question!!
        assertEquals("What is the capital of Assam?", q2.questionEn)
        assertEquals(0, q2.correctOptionIndex)
        assertEquals("Assam Geography", q2.subject)
    }

    @Test
    fun testSimplified11ColumnCsvParsing() {
        val sampleCsv = """
            question,a,b,c,d,correctAnswer,explanation,subject,topic,tags,difficulty
            "What is 15 * 6?","80","90","100","75","B","15 multiplied by 6 is 90.","General Mathematics","Arithmetic","Expected","Easy"
        """.trimIndent()

        val result = CsvQuestionParser.validateAndParseQuestions(
            csvText = sampleCsv
        )

        assertEquals(1, result.totalRows)
        assertEquals(1, result.validCount)
        val q = result.validRows[0].question!!
        assertEquals("What is 15 * 6?", q.questionEn)
        assertEquals("80", q.optionAEn)
        assertEquals("90", q.optionBEn)
        assertEquals(1, q.correctOptionIndex)
        assertEquals("General Mathematics", q.subject)
        assertEquals("Easy", q.difficulty)
    }

    @Test
    fun testInvalidCsvRowsDetection() {
        val invalidCsv = """
            Question in English,Question in Assamese,a,a_as,b,b_as,c,c_as,d,d_as,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty
            "","","A","","B","","C","","D","","A","","","Assam History","Topic","Tags","Medium"
            "Valid question without option B?","","Only Option A","","","","","","","","A","","","Assam History","Topic","Tags","Medium"
            "Question with invalid answer?","","Opt A","","Opt B","","Opt C","","Opt D","","Z","","","Assam History","Topic","Tags","Medium"
        """.trimIndent()

        val result = CsvQuestionParser.validateAndParseQuestions(
            csvText = invalidCsv
        )

        assertEquals(3, result.totalRows)
        assertEquals(0, result.validCount)
        assertEquals(3, result.invalidCount)

        assertTrue(result.invalidRows[0].errorReasons.any { it.contains("Missing Question text") })
        assertTrue(result.invalidRows[1].errorReasons.any { it.contains("Missing Option B") })
        assertTrue(result.invalidRows[2].errorReasons.any { it.contains("Invalid Correct Answer 'Z'") })
    }

    @Test
    fun testIntraBatchAndQuestionBankDuplicateDetection() {
        val existingQuestions = listOf(
            QuestionEntity(
                id = 555L,
                subject = "Assam History",
                topic = "Ahom Dynasty",
                difficulty = "Medium",
                questionEn = "Who was the first King of the Ahom Kingdom?",
                questionAs = "",
                optionAEn = "Sukaphaa",
                optionBEn = "Sutephaa",
                optionCEn = "Subinphaa",
                optionDEn = "Sudangphaa",
                optionAAs = "",
                optionBAs = "",
                optionCAs = "",
                optionDAs = "",
                correctOptionIndex = 0,
                explanationEn = "",
                explanationAs = ""
            )
        )

        val batchCsv = """
            Question in English,Question in Assamese,a,a_as,b,b_as,c,c_as,d,d_as,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty
            "Who was the first King of the Ahom Kingdom?","","Sukaphaa","","Sutephaa","","Subinphaa","","Sudangphaa","","A","","","Assam History","Ahom Dynasty","","Medium"
            "What is the largest river island in the world?","","Majuli","","Umananda","","Marajo","","Bananal","","A","Majuli is on Brahmaputra.","","Assam Geography","Rivers","","Easy"
            "What is the largest river island in the world?","","Majuli","","Umananda","","Marajo","","Bananal","","A","Duplicate row.","","Assam Geography","Rivers","","Easy"
        """.trimIndent()

        val result = CsvQuestionParser.validateAndParseQuestions(
            csvText = batchCsv,
            existingQuestions = existingQuestions
        )

        assertEquals(3, result.totalRows)
        // Row 1 exists in Q-Bank (valid, but flagged as existing in Q-Bank)
        val row1 = result.validRows.find { it.rowNumber == 2 }
        assertNotNull(row1)
        assertTrue(row1!!.isExistingInQBank)
        assertEquals(555L, row1.existingQBankId)

        // Row 2 is new & valid
        val row2 = result.validRows.find { it.rowNumber == 3 }
        assertNotNull(row2)
        assertFalse(row2!!.isExistingInQBank)

        // Row 3 is duplicate in batch
        val row3 = result.invalidRows.find { it.rowNumber == 4 }
        assertNotNull(row3)
        assertTrue(row3!!.isDuplicateInBatch)
        assertTrue(row3.errorReasons.any { it.contains("Duplicate question in batch") })
    }

    @Test
    fun testCsvWithEscapedQuotesAndCommas() {
        val complexCsv = """
            Question in English,Question in Assamese,a,a_as,b,b_as,c,c_as,d,d_as,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty
            "Who said, ""Swaraj is my birthright, and I shall have it""?","","Bal Gangadhar Tilak","","Lala Lajpat Rai","","Bipin Chandra Pal","","Gopal Krishna Gokhale","","A","Famous slogan by Lokmanya Tilak.","","Indian History","Freedom Struggle","Expected","Medium"
        """.trimIndent()

        val result = CsvQuestionParser.validateAndParseQuestions(complexCsv)
        assertEquals(1, result.validCount)
        val q = result.validRows[0].question!!
        assertEquals("Who said, \"Swaraj is my birthright, and I shall have it\"?", q.questionEn)
        assertEquals("Bal Gangadhar Tilak", q.optionAEn)
        assertEquals(0, q.correctOptionIndex)
    }
}
