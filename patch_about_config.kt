    val aboutConfig: StateFlow<AboutConfigEntity> = repository.aboutConfig.map {
        val config = it ?: SampleData.initialAboutConfig
        if (config.appTitle == "Jukti (যুক্তি)") {
            val newConfig = config.copy(appTitle = "Jukti")
            repository.updateAboutConfig(newConfig)
            newConfig
        } else {
            config
        }
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), SampleData.initialAboutConfig
    )
