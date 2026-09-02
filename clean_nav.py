import re

def fix(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # 1.
    bad1 = """                                        }
                                        }
                                    },"""
    good1 = """                                        }
                                    },"""
    content = content.replace(bad1, good1)
    
    # 2.
    bad2 = """                                }
                                }

                                Button("""
    good2 = """                                }

                                Button("""
    content = content.replace(bad2, good2)
    
    # 3.
    bad3 = """                                        }
                                        }
                                    }
                                ) {"""
    good3 = """                                        }
                                    }
                                ) {"""
    content = content.replace(bad3, good3)
    
    # 4.
    bad4 = """                                        }
                                        }
                                    )"""
    good4 = """                                        }
                                    )"""
    content = content.replace(bad4, good4)
    
    with open(filepath, 'w') as f:
        f.write(content)
        
fix("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
