sed -i 's/examDao.insertAll(SampleData.sampleExams)/syncManager.fetchAllExams()/g' app/src/main/java/com/example/data/repository/JuktiRepository.kt
