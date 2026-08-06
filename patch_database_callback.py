import re
with open("app/src/main/java/com/example/data/local/JuktiDatabase.kt", "r") as f:
    content = f.read()

content = content.replace("version = 19", "version = 20")

callback_import = """import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch"""

content = content.replace("import androidx.room.RoomDatabase", "import androidx.room.RoomDatabase\n" + callback_import)

callback_code = """
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JuktiDatabase::class.java,
                    "jukti_exam_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = database.notificationCategoryDao()
                                listOf("General", "Mock Test", "Exam Update", "Study Note", "Announcement").forEach {
                                    dao.insertNotificationCategory(NotificationCategoryEntity(name = it))
                                }
                            }
                        }
                    }
                })
                .build()
"""

content = content.replace("""                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JuktiDatabase::class.java,
                    "jukti_exam_db"
                ).fallbackToDestructiveMigration().build()""", callback_code)

with open("app/src/main/java/com/example/data/local/JuktiDatabase.kt", "w") as f:
    f.write(content)
