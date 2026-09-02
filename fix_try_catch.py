with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "r") as f:
    content = f.read()
    
old_block = """                            matchSubject && matchChapter
                        }
                    }

                    StudySubjectBannerCard("""

new_block = """                            matchSubject && matchChapter
                        } catch (e: Exception) {
                            false
                        }
                    }
                }
                
                if (!isStudySessionStarted) {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {

                    StudySubjectBannerCard("""

content = content.replace(old_block, new_block)
with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "w") as f:
    f.write(content)
