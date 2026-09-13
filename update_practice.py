path = "app/src/main/java/com/example/ui/screens/PracticeScreen.kt"
with open(path, "r") as f:
    lines = f.readlines()

start_idx = -1
for i, line in enumerate(lines):
    if "val bannersAndData by produceState(" in line:
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
    val bannersAndData = remember(precomputedBannerData, colorSurfaceVariant, colorPrimary) {
        val predefined = listOf(
            BannerConfig(
                titleEn = "General Knowledge",
                titleAs = "সাধাৰণ জ্ঞান",
                subtitleEn = "Assam history, geography, and more",
                subtitleAs = "অসমৰ ইতিহাস, ভূগোল আৰু অন্যান্য",
                subjectKey = "General Knowledge",
                icon = Icons.Default.Public,
                containerColor = colorSurfaceVariant,
                iconColor = colorPrimary
            ),
            BannerConfig(
                titleEn = "General English",
                titleAs = "সাধাৰণ ইংৰাজী",
                subtitleEn = "Grammar, vocabulary, and comprehension",
                subtitleAs = "ব্যাকৰণ, শব্দভাণ্ডাৰ আৰু বুজাপৰা",
                subjectKey = "General English",
                icon = Icons.Default.MenuBook,
                containerColor = colorSurfaceVariant,
                iconColor = colorPrimary
            ),
            BannerConfig(
                titleEn = "General Mathematics",
                titleAs = "সাধাৰণ গণিত",
                subtitleEn = "Arithmetic, algebra, and geometry",
                subtitleAs = "পাটিগণিত, বীজগণিত আৰু জ্যামিতি",
                subjectKey = "General Mathematics",
                icon = Icons.Default.Calculate,
                containerColor = colorSurfaceVariant,
                iconColor = colorPrimary
            ),
            BannerConfig(
                titleEn = "Reasoning & Mental Ability",
                titleAs = "যুক্তিবিদ্যা (Reasoning)",
                subtitleEn = "Logical and analytical reasoning",
                subtitleAs = "যৌক্তিক আৰু বিশ্লেষণাত্মক যুক্তি",
                subjectKey = "Reasoning & Mental Ability",
                icon = Icons.Default.Psychology,
                containerColor = colorSurfaceVariant,
                iconColor = colorPrimary
            ),
            BannerConfig(
                titleEn = "Transport & Motor Vehicle",
                titleAs = "পৰিবহন আৰু মটৰ বাহন",
                subtitleEn = "Motor vehicle act and traffic signs",
                subtitleAs = "মটৰ বাহন আইন আৰু যান-বাহনৰ সংকেত",
                subjectKey = "Transport & Motor Vehicle",
                icon = Icons.Default.Traffic,
                containerColor = colorSurfaceVariant,
                iconColor = colorPrimary
            )
        )

        val banners = predefined + listOf(
            BannerConfig(
                titleEn = "All Subjects",
                titleAs = "সকলো বিষয়",
                subtitleEn = "Mixed questions from all subjects",
                subtitleAs = "সকলো বিষয়ৰ পৰা মিশ্ৰিত প্ৰশ্ন",
                subjectKey = "All Subjects",
                icon = Icons.Default.AllInclusive,
                containerColor = colorSurfaceVariant,
                iconColor = colorPrimary
            )
        )
        
        val bannerDataMap = banners.associate { banner ->
            banner to (precomputedBannerData[banner.subjectKey] ?: Triple(emptyList(), emptyList(), emptyMap()))
        }
        
        banners to bannerDataMap
    }
"""
        new_lines = lines[:start_idx] + [replacement] + lines[end_idx+1:]
        with open(path, "w") as f:
            f.writelines(new_lines)
        print("Success PracticeScreen")
    else:
        print("Could not find end of block PracticeScreen")
else:
    print("Could not find start of block PracticeScreen")

