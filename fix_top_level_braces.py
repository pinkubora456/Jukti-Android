import re

def fix(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # 1. Before PracticeSummaryView
    old1 = """        }
    }

@Composable
fun PracticeSummaryView("""
    new1 = """        }
    }
}

@Composable
fun PracticeSummaryView("""
    content = content.replace(old1, new1)

    # 2. Before SummaryStatCard
    old2 = """        }
    }

@Composable
fun SummaryStatCard("""
    new2 = """        }
    }
}

@Composable
fun SummaryStatCard("""
    content = content.replace(old2, new2)

    # 3. End of file
    old3 = """            Text(text = title, style = MaterialTheme.typography.labelMedium, color = color)
        }
    }"""
    new3 = """            Text(text = title, style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}"""
    content = content.replace(old3, new3)

    with open(filepath, 'w') as f:
        f.write(content)

fix("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
