sed -i 's/finalQuestionType = if (baseExam.isNotBlank()) "PYQ - $baseExam" else "PYQ"/finalQuestionType = "PYQ"/g' app/src/main/java/com/example/data/repository/JuktiRepository.kt
