sed -i 's/if (q.questionType.startsWith("PYQ", ignoreCase = true)) {/if (q.questionType.startsWith("PYQ", ignoreCase = true)) { questionTag = "PYQ" } else { questionTag = q.questionType.ifBlank { "Expected" } }/g' app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt
sed -i '/questionTag = "PYQ"/d' app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt
