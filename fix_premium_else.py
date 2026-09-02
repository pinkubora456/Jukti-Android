import re

def fix(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    bad = """                                        Button(onClick = { viewModel.showPaywall() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                                            Text("Unlock Premium")
               
           
       
                                        }
                                    }
                                }
                            }
                                } else {"""
                                
    good = """                                        Button(onClick = { viewModel.showPaywall() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                                            Text("Unlock Premium")
                                        }
                                    }
                                }
                            } else {"""
                            
    content = content.replace(bad, good)
    
    with open(filepath, 'w') as f:
        f.write(content)
        
fix("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
