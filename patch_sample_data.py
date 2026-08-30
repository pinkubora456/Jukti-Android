import re

with open("app/src/main/java/com/example/data/repository/SampleData.kt", "r") as f:
    content = f.read()

# Replace sampleExamUpdates with empty list
content = re.sub(r'val sampleExamUpdates = listOf\([\s\S]*?\n    val sampleBanners', 'val sampleExamUpdates = emptyList<ExamUpdateEntity>()\n    val sampleBanners', content)

# Replace sampleBanners with empty list
content = re.sub(r'val sampleBanners = listOf\([\s\S]*?\n    val sampleNotifications', 'val sampleBanners = emptyList<BannerEntity>()\n    val sampleNotifications', content)

# Replace sampleSubjectsChapters with empty list
content = re.sub(r'val sampleSubjectsChapters = listOf\([\s\S]*?\n    val initialUserProfile', 'val sampleSubjectsChapters = emptyList<SubjectChapterEntity>()\n    val initialUserProfile', content)

# Remove ADRE 2022 Qualifier
content = content.replace('founderCredential = "ADRE 2022 Qualifier"', 'founderCredential = ""')

with open("app/src/main/java/com/example/data/repository/SampleData.kt", "w") as f:
    f.write(content)
