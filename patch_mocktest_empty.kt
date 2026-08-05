                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        EmptyStateIllustration(
                            type = com.example.ui.components.EmptyStateType.STUDENT_JAAPI,
                            title = if (isAssamese) "কোনো মক টেষ্ট পোৱা নগ'ল" else "No Mock Tests Found",
                            message = if (isAssamese) "অনুগ্ৰহ কৰি আপোনাৰ চাৰ্চ বা ফিল্টাৰ সলনি কৰক" else "Try clearing your search query or filters",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
