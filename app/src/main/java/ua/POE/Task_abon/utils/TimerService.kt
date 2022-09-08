package ua.POE.Task_abon.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ua.POE.Task_abon.R
import java.util.*

class TimerService: Service() {

    private val scope = CoroutineScope(Dispatchers.Main)
    private val timer = Timer()

    override fun onCreate() {
        super.onCreate()
        createChanel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        scope.cancel()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        scope.launch {
            val time = intent.getIntExtra(TIME_EXTRA, 0)
            timer.scheduleAtFixedRate(TimeTask(time), 0, 1000)
        }
        return START_NOT_STICKY
    }

    private fun createChanel() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANEL_ID, NAME, NotificationManager.IMPORTANCE_NONE)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() =
        NotificationCompat.Builder(this, CHANEL_ID)
            .setContentTitle("Task_abon")
            .setContentText("Работа с ведомостью")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .build()

    private inner class TimeTask(private var time: Int) : TimerTask() {
        override fun run() {
            val intent = Intent(TIMER_UPDATED)
            time++
            intent.putExtra(TIME_EXTRA, time)
            sendBroadcast(intent)
        }

    }

    override fun onBind(p0: Intent?): IBinder? = null

    companion object {
        const val NAME = "timer"
        const val CHANEL_ID = "chanel_id"
        const val NOTIFICATION_ID = 12

        const val TIMER_UPDATED = "timerUpdater"
        const val TIME_EXTRA = "timeExtra"

        fun getIntent(context: Context, time: Int): Intent {
            return Intent(context, TimerService::class.java).apply {
                putExtra(TIME_EXTRA, time)
            }
        }
    }
}