with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "r") as f:
    repo = f.read()

repo = repo.replace(
    "    private val _premiumExamUpdates = kotlinx.coroutines.flow.MutableStateFlow<List<ExamUpdateEntity>>(emptyList())\n    val premiumExamUpdates: kotlinx.coroutines.flow.StateFlow<List<ExamUpdateEntity>> = _premiumExamUpdates.asStateFlow()",
    ""
)

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "w") as f:
    f.write(repo)
