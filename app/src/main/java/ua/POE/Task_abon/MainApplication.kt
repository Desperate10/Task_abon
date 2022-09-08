package ua.POE.Task_abon

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import ua.POE.Task_abon.data.AppDatabase

@HiltAndroidApp
class MainApplication : Application()