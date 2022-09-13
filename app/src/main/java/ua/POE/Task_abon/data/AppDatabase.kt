package ua.POE.Task_abon.data

import android.content.Context
import android.util.Log
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ua.POE.Task_abon.data.dao.*
import ua.POE.Task_abon.data.entities.*

@Database(
    entities = [TestEntity::class, Task::class, Directory::class, Catalog::class, UserData::class, Result::class, Timing::class],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    abstract fun catalogDao(): CatalogDao

    abstract fun directoryDao(): DirectoryDao

    abstract fun testEntityDao(): TestEntityDao

    abstract fun resultDao(): ResultDao

    abstract fun timingDao(): TimingDao

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    val catalogDao = database.catalogDao()
                    catalogDao.deleteTypeOne()
                    catalogDao.insert(Catalog(0, "0", "0", "Виконано"))
                    catalogDao.insert(Catalog(0, "0", "1", "Не виконано"))
                    catalogDao.insert(Catalog(0, "1", "1", "Містить"))
                    catalogDao.insert(Catalog(0, "1", "2", "Рівно"))
                    catalogDao.insert(Catalog(0, "1", "3", "Починається з"))
                    catalogDao.insert(Catalog(0, "1", "4", "Не пусте"))
                    catalogDao.insert(Catalog(0, "1", "5", "Пусте"))
                    /*val passwordDao = database.passwordDao()
                    //чистим пароли
                    passwordDao.deleteAll()
                    //добавляем
                    var password = Password("123")
                    passwordDao.insert(password)
                    password = Password("321")
                    passwordDao.insert(password)*/
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE result ADD COLUMN lat TEXT")
                database.execSQL("ALTER TABLE result ADD COLUMN lng TEXT")
                database.execSQL("ALTER TABLE result ADD COLUMN numbpers TEXT")
                database.execSQL("ALTER TABLE result ADD COLUMN family TEXT")
                database.execSQL("ALTER TABLE result ADD COLUMN adress TEXT")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE result ADD COLUMN point_condition TEXT")
                database.execSQL("ALTER TABLE result ADD COLUMN old_tel TEXT")
                database.execSQL("ALTER TABLE result ADD COLUMN is_main INTEGER")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE result ADD COLUMN photo TEXT")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE UserData ADD COLUMN Counter_numb TEXT")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE UserData ADD COLUMN opora TEXT")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE UserData ADD COLUMN icons_account TEXT")
                database.execSQL("ALTER TABLE UserData ADD COLUMN icons_counter TEXT")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context, scope).also { INSTANCE = it }
            }

        private fun buildDatabase(appContext: Context, scope: CoroutineScope) =
            Room.databaseBuilder(appContext, AppDatabase::class.java, "app_database")
                .addMigrations(
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8
                )
                .fallbackToDestructiveMigration()
                .addCallback(AppDatabaseCallback(scope))
                .allowMainThreadQueries()
                .build()
    }
}