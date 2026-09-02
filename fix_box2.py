def fix(filepath):
    with open(filepath, 'r') as f:
        lines = f.readlines()
        
    for i in range(1110, 1130):
        if "Box(contentAlignment = Alignment.Center) {" in lines[i]:
            start = i
            break
            
    # lines to replace:
    # 1115                                     Box(contentAlignment = Alignment.Center) {
    # 1116                                         if (isCorrect) {
    # 1117                                             Icon(Icons.Default.Check, contentDescription = "Correct Answer", tint = Color.White, modifier = Modifier.size(18.dp))
    # 1118                                         }
    # 1119                                     }
    # 1120                                 }
    # 1121              else if (isSelected && !isCorrect) {
    # 1122                                             Icon(Icons.Default.Close, contentDescription = "Incorrect Option", tint = Color.White, modifier = Modifier.size(18.dp))
    # 1123              } else {
    # 1124                                             Text(
    # 1125                                                 text = optionLetter,
    # 1126                                                 style = MaterialTheme.typography.labelMedium,
    # 1127                                                 fontWeight = FontWeight.Bold,
    # 1128                                                 color = MaterialTheme.colorScheme.onPrimaryContainer
    # 1129                                             )
    # 1130             
    lines[start:start+16] = [
        "                                    Box(contentAlignment = Alignment.Center) {\n",
        "                                        if (isCorrect) {\n",
        "                                            Icon(Icons.Default.Check, contentDescription = \"Correct Answer\", tint = Color.White, modifier = Modifier.size(18.dp))\n",
        "                                        } else if (isSelected && !isCorrect) {\n",
        "                                            Icon(Icons.Default.Close, contentDescription = \"Incorrect Option\", tint = Color.White, modifier = Modifier.size(18.dp))\n",
        "                                        } else {\n",
        "                                            Text(\n",
        "                                                text = optionLetter,\n",
        "                                                style = MaterialTheme.typography.labelMedium,\n",
        "                                                fontWeight = FontWeight.Bold,\n",
        "                                                color = MaterialTheme.colorScheme.onPrimaryContainer\n",
        "                                            )\n",
        "                                        }\n",
        "                                    }\n"
    ]
    with open(filepath, 'w') as f:
        f.writelines(lines)
    print("Fixed box!")

fix("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt")
