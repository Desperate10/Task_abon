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
import ua.POE.Task_abon.data.dao.impl.TaskCustomerDaoImpl
import ua.POE.Task_abon.presentation.model.Icons
import ua.POE.Task_abon.utils.XmlLoader
import ua.POE.Task_abon.utils.getRawTextFile
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob())
    }

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext appContext: Context, scope: CoroutineScope) =
        AppDatabase.getDatabase(appContext, scope)

    @Singleton
    @Provides
    fun provideXmlLoader(
        @ApplicationContext appContext: Context,
        provideDatabase: AppDatabase,
        taskDao: TaskDao,
        directoryDao: DirectoryDao,
        catalogDao: CatalogDao
    ) = XmlLoader(appContext, provideDatabase, taskDao, directoryDao, catalogDao)

    @Singleton
    @Provides
    fun provideTaskDao(db: AppDatabase): TaskDao {
        return db.taskDao()
    }

    @Singleton
    @Provides
    fun provideDirectoryDao(db: AppDatabase): DirectoryDao {
        return db.directoryDao()
    }


    @Singleton
    @Provides
    fun provideCatalogDao(db: AppDatabase): CatalogDao {
        return db.catalogDao()
    }

    @Singleton
    @Provides
    fun provideTaskCustomerDao(db: AppDatabase): TaskCustomerDao {
        return db.taskCustomerDao()
    }

    @Singleton
    @Provides
    fun provideTaskCustomersDaoImpl(db: AppDatabase,  taskCustomerDao: TaskCustomerDao): TaskCustomerDaoImpl {
        return TaskCustomerDaoImpl(db, taskCustomerDao)
    }

    @Singleton
    @Provides
    fun provideResultDao(db: AppDatabase): ResultDao {
        return db.resultDao()
    }

    @Singleton
    @Provides
    fun provideTimingDao(db: AppDatabase): TimingDao {
        return db.timingDao()
    }

    @Provides
    fun getIcons(@ApplicationContext appContext: Context): List<Icons> {
        return appContext.resources.getRawTextFile(R.raw.icons)
    }
}