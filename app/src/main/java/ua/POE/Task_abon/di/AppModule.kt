package ua.POE.Task_abon.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import ua.POE.Task_abon.R
import ua.POE.Task_abon.data.AppDatabase
import ua.POE.Task_abon.data.dao.*
import ua.POE.Task_abon.data.repository.*
import ua.POE.Task_abon.domain.model.Icons
import ua.POE.Task_abon.utils.XmlLoader
import ua.POE.Task_abon.utils.getRawTextFile
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun getIcons(@ApplicationContext appContext : Context) : List<Icons> {
        return appContext.resources.getRawTextFile(R.raw.icons)
    }

    @Provides
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob())
    }

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext appContext : Context, scope: CoroutineScope) = AppDatabase.getDatabase(appContext, scope)

    @Singleton
    @Provides
    fun provideXmlLoader(@ApplicationContext appContext: Context, provideDatabase: AppDatabase,taskDao: TaskDao, directoryDao: DirectoryDao, catalogDao: CatalogDao, resultDao: ResultDao) = XmlLoader(appContext,provideDatabase, taskDao, directoryDao, catalogDao, resultDao)

    @Singleton
    @Provides
    fun provideTaskDao(db: AppDatabase) :TaskDao {
        return db.taskDao()
    }

    @Singleton
    @Provides
    fun provideTaskRepository(xmlLoader: XmlLoader, localDataSource : TaskDao) = TaskRepository(xmlLoader, localDataSource)


    @Singleton
    @Provides
    fun provideTaskDetailRepository(localDataSource: TestEntityDao, directoryDao: DirectoryDao, resultDao: ResultDao) = TaskDetailRepository(localDataSource, directoryDao, resultDao)

    @Singleton
    @Provides
    fun provideDirectoryDao(db: AppDatabase) :DirectoryDao {
        return db.directoryDao()
    }

    @Singleton
    @Provides
    fun provideDirectoryRepository(localDataSource : DirectoryDao) = DirectoryRepository(localDataSource)

    @Singleton
    @Provides
    fun provideCatalogDao(db: AppDatabase) : CatalogDao {
        return db.catalogDao()
    }

    @Singleton
    @Provides
    fun provideTestEntityDao(db: AppDatabase) : TestEntityDao {
        return db.testEntityDao()
    }

    @Singleton
    @Provides
    fun provideTestEntityRepository(appDatabase: AppDatabase) = TestEntityRepository(appDatabase)

    @Singleton
    @Provides
    fun provideResultDao(db : AppDatabase) : ResultDao {
        return db.resultDao()
    }

    @Singleton
    @Provides
    fun provideResultRepository(resultDao: ResultDao) = ResultRepository(resultDao)

    @Singleton
    @Provides
    fun provideTimingDao(db: AppDatabase) : TimingDao {
        return db.timingDao()
    }

    @Singleton
    @Provides
    fun provideTimingRepository(timingDao: TimingDao) = TimingRepository(timingDao)

}