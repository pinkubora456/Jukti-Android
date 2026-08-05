            if (filteredNotes.isEmpty()) {
                com.example.ui.components.EmptyStateIllustration(
                    type = com.example.ui.components.EmptyStateType.NOTEBOOK_GAMOSA,
                    title = if (isAssamese) "কোনো টোকা পোৱা নগ'ল" else "No Notes Found",
                    message = if (isAssamese) "অনুগ্ৰহ কৰি আপোনাৰ ফিল্টাৰ সলনি কৰক" else "Try clearing your search query or filters",
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredNotes) { note ->
                        StudyNoteListItem(
                            note = note,
                            language = language,
                            onClick = { viewModel.selectStudyNote(note) },
                            onBookmarkToggle = { viewModel.toggleBookmarkNote(note) },
                            onDownloadToggle = { viewModel.toggleDownloadNote(note) }
                        )
                    }
                }
            }
