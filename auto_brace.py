import re

def restore_braces(filepath):
    with open(filepath, 'r') as f:
        lines = f.readlines()

    out_lines = []
    
    # We will just parse the file and count '{' and '}'
    # But wait, we can just run the Kotlin compiler and parse the errors!
    # The Kotlin compiler says: "1022: Syntax error: Expecting ')'"
    # Wait, the compiler errors are very robust!
    
