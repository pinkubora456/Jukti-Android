        Crossfade(targetState = selectedTab, label = "TabSwitch") { tab ->
            if (tab == 0) {
                // STATE LEADERBOARD TAB (HERO BAR, OVERALL VS SAME EXAM, TOP 3 PODIUM, DROPDOWN & RANK LIST)
                LeaderboardTabContent(
                    userXp = userProfile?.xp ?: 2350,
                    userLevel = userProfile?.level ?: 8,
                    userMockAvg = mockAvg,
                    isAssamese = isAssamese
                )
            } else {
                // MY ANALYTICS TAB (REBUILT WITH ALL NEW USER REQUIREMENTS)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // 1. PROBABILITY OF CLEARING EXAM IN %
                    ExamClearanceProbabilityCard(
                        isAssamese = isAssamese,
                        mockHistoryList = mockHistoryList,
                        subjectBreakdownList = subjectBreakdownList,
                        userProfile = userProfile
                    )

                    // 2. KEY PERFORMANCE INDICATOR (KPI) IN 2x2 GRID
                    KpiGrid2x2(userProfileSolved = userProfile?.totalSolved ?: 1248, isAssamese = isAssamese)

                    // 3. WEAK SUBJECT FOCUS
                    WeakSubjectFocusSection(
                        subjectBreakdownList = subjectBreakdownList,
                        isAssamese = isAssamese,
                        onPracticeClick = { viewModel.navigateTo(Screen.PRACTICE) }
                    )

                    // 4. SUBJECT ACCURACY BREAKDOWN (Banner + Dropdown arrow + Chapters lowest-to-highest + NA at last + Missed Questions button)
                    SubjectAccuracyBreakdownSection(
                        subjectBreakdownList = subjectBreakdownList,
                        isAssamese = isAssamese,
                        onCheckMissedQuestions = { subject ->
                            activeMissedQuestionSubject = subject
                        }
                    )

                    // 5. MOCKTEST SCORE TREND IN LINE GRAPH
                    MockTestScoreTrendCard(isAssamese = isAssamese)

                    // 5.1. STUDY TIME TREND IN LINE GRAPH
                    StudyTimeTrendCard(isAssamese = isAssamese)

                    // 6. MOCK HISTORY
                    MockTestHistorySection(
                        mockHistoryList = mockHistoryList,
                        isAssamese = isAssamese,
                        onViewMockResult = { mockItem ->
                            viewModel.analyzeMockFromHistory(
                                titleEn = mockItem.titleEn,
                                titleAs = mockItem.titleAs,
                                score = mockItem.score,
                                totalMarks = mockItem.totalMarks,
                                accuracy = mockItem.accuracy,
                                attempted = mockItem.attempted,
                                correct = mockItem.correct,
                                incorrect = mockItem.incorrect,
                                totalQuestions = mockItem.totalMarks
                            )
                        }
                    )
                }
            }
        }
