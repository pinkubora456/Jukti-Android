package com.example

import com.example.data.local.QuestionEntity
import com.example.data.repository.normalizeChapterName
import com.example.data.repository.normalizeQuestionEntity
import com.example.data.repository.normalizeSubjectName
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for chapter normalization, transport rule mapping, manual entry merging, and data integrity.
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testChapterNormalization() {
    val variants = listOf(
      "One-Word & Idiom/Phrase",
      "Idioms, Phrases & One-Word Substitution",
      "One-Word & Idiom / Phrase",
      "One-Word & Idiom / Phrases",
      "Idiom/Phrase & One-Word",
      "One-Word Substitution",
      "One-Word & Idiom",
      "One-Word & Idioms",
      "Idioms & Phrases",
      "Idioms",
      "Idiom",
      "Phrases & Idioms",
      "One-Word",
      "One Word Substitution"
    )
    for (v in variants) {
      assertEquals("One-Word & Idioms", normalizeChapterName(v, "General English"))
    }
  }

  @Test
  fun testSubjectNormalization() {
    val manualVariants = listOf(
      "Manual Entry",
      "manual entry",
      "Manual",
      "manual",
      "MANUAL ENTRY",
      "Transport Rules",
      "transport rules",
      "Transport Rule"
    )
    for (v in manualVariants) {
      assertEquals("Transport & Motor Vehicle", normalizeSubjectName(v))
    }

    assertEquals("General Knowledge", normalizeSubjectName("General Knowledge"))
    assertEquals("General English", normalizeSubjectName("General English"))
    assertEquals("General Mathematics", normalizeSubjectName("General Mathematics"))
    assertEquals("Reasoning & Mental Ability", normalizeSubjectName("Reasoning"))
  }

  @Test
  fun testQuestionEntityNormalization() {
    val legacyQuestion = QuestionEntity(
      id = 100L,
      subject = "General English",
      topic = "One-Word & Idiom/Phrase",
      difficulty = "Medium",
      questionEn = "A person who loves books is called?",
      questionAs = "যি ব্যক্তিয়ে কিতাপ ভাল পায় তাক কি কোৱা হয়?",
      optionAEn = "Bibliophile",
      optionBEn = "Philologist",
      optionCEn = "Scholar",
      optionDEn = "Pedant",
      optionAAs = "বিব্লিঅ'ফাইল",
      optionBAs = "ফিল'লজিষ্ট",
      optionCAs = "পণ্ডিত",
      optionDAs = "পেডান্ট",
      correctOptionIndex = 0,
      explanationEn = "A bibliophile is someone with a great love for books.",
      explanationAs = "বিব্লিঅ'ফাইল হৈছে এনে এজন ব্যক্তি যি কিতাপ ভাল পায়।",
      examCategory = "ADRE Grade III",
      isPremium = false,
      questionType = "Expected"
    )

    val normalized = normalizeQuestionEntity(legacyQuestion)
    assertEquals("General English", normalized.subject)
    assertEquals("One-Word & Idioms", normalized.topic)
    assertEquals(100L, normalized.id)
    assertEquals("Bibliophile", normalized.optionAEn)
    assertEquals(0, normalized.correctOptionIndex)
  }

  @Test
  fun testManualEntryQuestionNormalization() {
    val manualQuestion = QuestionEntity(
      id = 150L,
      subject = "Manual Entry",
      topic = "Driving Regulations, Licences & Permits",
      difficulty = "Medium",
      questionEn = "What is the minimum age to apply for a driving licence for a transport vehicle in India?",
      questionAs = "ভাৰতত পৰিবহণ বাহনৰ ড্ৰাইভিং লাইচেন্সৰ বাবে আবেদন কৰাৰ ন্যূনতম বয়স কিমান?",
      optionAEn = "18 years",
      optionBEn = "20 years",
      optionCEn = "21 years",
      optionDEn = "25 years",
      optionAAs = "১৮ বছৰ",
      optionBAs = "২০ বছৰ",
      optionCAs = "২১ বছৰ",
      optionDAs = "২৫ বছৰ",
      correctOptionIndex = 1,
      explanationEn = "Under the Motor Vehicles Act, 20 years is the minimum age for a transport vehicle driving licence.",
      explanationAs = "মটৰ বাহন আইন অনুসৰি পৰিবহণ বাহনৰ ড্ৰাইভিং লাইচেন্সৰ বাবে ন্যূনতম বয়স ২০ বছৰ।",
      examCategory = "Transport Department Exam",
      isPremium = false,
      questionType = "Expected"
    )

    val normalized = normalizeQuestionEntity(manualQuestion)
    assertEquals("Transport & Motor Vehicle", normalized.subject)
    assertEquals("Motor Vehicles Act & Traffic Rules", normalized.topic)
  }

  @Test
  fun testTransportRuleNormalization() {
    val transportQuestion = QuestionEntity(
      id = 200L,
      subject = "Transport Rules",
      topic = "Traffic Signs, Signals & Road Safety",
      difficulty = "Easy",
      questionEn = "What does a red traffic light indicate?",
      questionAs = "ৰঙা ট্ৰেফিক লাইটে কি নিৰ্দেশ কৰে?",
      optionAEn = "Stop",
      optionBEn = "Go",
      optionCEn = "Slow down",
      optionDEn = "Turn right",
      optionAAs = "ৰওক",
      optionBAs = "যাওক",
      optionCAs = "গতি কমাওক",
      optionDAs = "সোঁফালে ঘূৰক",
      correctOptionIndex = 0,
      explanationEn = "A red light means vehicles must come to a complete stop.",
      explanationAs = "ৰঙা লাইটৰ অৰ্থ হ'ল বাহনসমূহ সম্পূৰ্ণৰূপে ৰ'ব লাগিব।",
      examCategory = "Transport Department Exam",
      isPremium = false,
      questionType = "Expected"
    )

    val normalized = normalizeQuestionEntity(transportQuestion)
    assertEquals("Transport & Motor Vehicle", normalized.subject)
    assertEquals("Traffic Signs, Signals & Road Safety", normalized.topic)
  }
}


