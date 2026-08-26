import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

target = """    // Data Flows from Repository
    val plans = repository.allPlans.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val examsList = repository.allExams.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val bannerUrl = repository.bannerUrl.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), ""
    )
    val allQuestionsList = repository.allQuestions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )"""

replacement = """    // Data Flows from Repository
    val plans = repository.allPlans.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val examsList = repository.allExams.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val bannerUrl = repository.bannerUrl.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), ""
    )
    val allQuestionsList = repository.allQuestions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )"""

content = content.replace(target, replacement)
