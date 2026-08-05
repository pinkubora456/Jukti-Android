        viewModelScope.launch {
            userProfile.collect { prof ->
                if (prof != null) {
                    if (splashFinished) {
                        if (!prof.isLoggedIn) {
                            if (_currentScreen.value != Screen.AUTH) {
                                _currentScreen.value = Screen.AUTH
                            }
                        } else {
                            if (_currentScreen.value == Screen.AUTH || _currentScreen.value == Screen.SPLASH) {
                                _currentScreen.value = Screen.HOME
                            }
                            if (prof.email.isNotBlank() && prof.currentDeviceId.isNotBlank()) {
                                val activeInManager = UserSessionManager.getActiveDeviceId(prof.email)
                                if (activeInManager == null) {
                                    UserSessionManager.registerSession(prof.email, prof.currentDeviceId)
                                } else if (activeInManager != prof.currentDeviceId) {
                                    logoutDueToOtherDeviceLogin()
                                }
                            }
                        }
                    }
                }
            }
        }
