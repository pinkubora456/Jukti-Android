path = "app/src/main/java/com/example/ui/screens/McqStudyScreen.kt"
with open(path, "r") as f:
    lines = f.readlines()

start_idx = -1
for i, line in enumerate(lines):
    if "val studyBannerData by produceState(" in line:
        start_idx = i
        break

if start_idx != -1:
    end_idx = -1
    open_braces = 0
    in_block = False
    
    for i in range(start_idx, len(lines)):
        open_braces += lines[i].count('{')
        open_braces -= lines[i].count('}')
        if "{" in lines[i]:
            in_block = True
        
        if in_block and open_braces == 0:
            end_idx = i
            break
            
    if end_idx != -1:
        replacement = """    val precomputedBannerData by viewModel.precomputedBannerData.collectAsStateWithLifecycle()
    val studyBannerData = remember(precomputedBannerData) {
        val map = mutableMapOf<String, Triple<List<String>, Int, Map<String, Int>>>()
        for (banner in studyBanners) {
            val data = precomputedBannerData[banner.subjectKey]
            if (data != null) {
                map[banner.subjectKey] = Triple(data.first, data.second.size, data.third)
            }
        }
        map
    }
"""
        new_lines = lines[:start_idx] + [replacement] + lines[end_idx+1:]
        with open(path, "w") as f:
            f.writelines(new_lines)
        print("Success McqStudyScreen")
    else:
        print("Could not find end of block McqStudyScreen")
else:
    print("Could not find start of block McqStudyScreen")

