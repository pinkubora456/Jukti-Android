with open("app/src/main/java/com/example/ui/screens/ExamInfoScreen.kt", "r") as f:
    content = f.read()

old_block = """        // CONTENT SECTION BASED ON HERO TAB SELECTION
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedHeroTab) {
                HeroTab.SYLLABUS -> {
                    item {
                        SyllabusHeroHeader(isAssamese)
                    }
                    if (selectedExamTab == "All" || selectedExamTab == "ADRE") {
                        item { AdreSyllabusCard(isAssamese) }
                    }
                    if (selectedExamTab == "All" || selectedExamTab == "APSC") {
                        item { ApscSyllabusCard(isAssamese) }
                    }
                    if (selectedExamTab == "All" || selectedExamTab == "Assam Police") {
                        item { AssamPoliceSyllabusCard(isAssamese) }
                    }
                    if (selectedExamTab == "All" || selectedExamTab == "TET") {
                        item { TetSyllabusCard(isAssamese) }
                    }
                }

                HeroTab.PATTERN -> {
                    item {
                        ExamPatternHeroHeader(isAssamese)
                    }
                    if (selectedExamTab == "All" || selectedExamTab == "ADRE") {
                        item { AdreExamPatternCardDetailed(isAssamese) }
                    }
                    if (selectedExamTab == "All" || selectedExamTab == "APSC") {
                        item { ApscExamPatternCardDetailed(isAssamese) }
                    }
                    if (selectedExamTab == "All" || selectedExamTab == "Assam Police") {
                        item { AssamPoliceExamPatternCardDetailed(isAssamese) }
                    }
                    if (selectedExamTab == "All" || selectedExamTab == "TET") {
                        item { TetExamPatternCardDetailed(isAssamese) }
                    }
                }

                HeroTab.CUTOFF -> {
                    item {
                        CutoffHeroHeader(isAssamese)
                    }
                    if (selectedExamTab == "All" || selectedExamTab == "ADRE") {
                        item { AdreCutoffCard(isAssamese) }
                    }
                    if (selectedExamTab == "All" || selectedExamTab == "APSC") {
                        item { ApscCutoffCard(isAssamese) }
                    }
                    if (selectedExamTab == "All" || selectedExamTab == "Assam Police") {
                        item { AssamPoliceCutoffCard(isAssamese) }
                    }
                    if (selectedExamTab == "All" || selectedExamTab == "TET") {
                        item { TetCutoffCard(isAssamese) }
                    }
                }
            }

            // DYNAMIC UPDATES LIST (RELEVANT NOTICES FROM DATABASE)
            if (filteredUpdates.isNotEmpty()) {
                item {
                    Text(
                        text = "Official Notices & Notifications:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                items(filteredUpdates) { update ->
                    ExamUpdateItemCard(
                        update = update,
                        language = language,
                        onOpenLink = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(update.officialLink))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }"""

new_block = """        // CONTENT SECTION BASED ON HERO TAB SELECTION
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedHeroTab) {
                HeroTab.SYLLABUS -> {
                    item {
                        SyllabusHeroHeader(isAssamese)
                    }
                }
                HeroTab.PATTERN -> {
                    item {
                        ExamPatternHeroHeader(isAssamese)
                    }
                }
                HeroTab.CUTOFF -> {
                    item {
                        CutoffHeroHeader(isAssamese)
                    }
                }
            }

            if (filteredUpdates.isNotEmpty()) {
                items(filteredUpdates) { update ->
                    ExamUpdateItemCard(
                        update = update,
                        language = language,
                        onOpenLink = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(update.officialLink))
                            context.startActivity(intent)
                        }
                    )
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No entries found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No syllabus, exam pattern, or cutoff details have been added yet for this filter. Admin users can add or update entries via the top-right manage icon.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }"""

if old_block in content:
    content = content.replace(old_block, new_block)
    with open("app/src/main/java/com/example/ui/screens/ExamInfoScreen.kt", "w") as f:
        f.write(content)
    print("Successfully removed dummy cards from ExamInfoScreen.kt")
else:
    print("Error: old_block not found in ExamInfoScreen.kt")
