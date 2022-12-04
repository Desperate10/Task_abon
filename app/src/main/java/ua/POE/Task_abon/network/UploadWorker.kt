package ua.POE.Task_abon.network

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ua.POE.Task_abon.R
import ua.POE.Task_abon.utils.getFileName
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * WorkManager for photoUploading
 * */
@HiltWorker
class UploadWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val ioDispatcher: CoroutineDispatcher
) : CoroutineWorker(context, workerParameters), UploadRequestBody.UploadCallback {

    private val builder = NotificationCompat.Builder(context, CHANNEL_ID)

    private fun createNotification(percentage: Int, fileName: String) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        builder
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(fileName)
            .setContentText("Вигрузка фото...")
            .setProgress(100, percentage, false)
            .setOngoing(true)
            .setAutoCancel(true)
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID,builder.build())
    }

    override suspend fun doWork(): Result  {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME,NotificationManager.IMPORTANCE_LOW)
            notificationManager?.createNotificationChannel(channel)
        }

        return withContext(ioDispatcher) {
            val inputPhotoUri = inputData.getStringArray(URI_ARRAY)
            try {
                uploadImages(inputPhotoUri?.toList() ?: emptyList())
            } catch (e: IOException) {
                Result.failure()
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }


    private fun uploadImages(uriStrings: List<String>): Result {

        if (uriStrings.isEmpty()) {
            return Result.failure()
        }

        var result = Result.failure()

        val list: ArrayList<MultipartBody.Part> = ArrayList()
        for (i in uriStrings.indices) {
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(
                uriStrings[i].toUri(), "r", null
            ) ?: return Result.failure()

            val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
            val file = File(
                context.cacheDir, context.contentResolver.getFileName(
                    uriStrings[i].toUri()
                )
            )
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            val body = UploadRequestBody(file, "image", this)
            list.add(prepareFilePart(uriStrings[i].toUri(), body))
            parcelFileDescriptor.close()
        }

        MyApi().uploadImage(
            list, RequestBody.create(MediaType.parse("multipart/form-data"), "json")
        ).enqueue(object : Callback<UploadResponse> {
            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                builder.setContentText("Помилка")
                NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
                result = Result.failure()
            }

            override fun onResponse(
                call: Call<UploadResponse>, response: Response<UploadResponse>
            ) {
                response.body()?.let {
                    if (response.code().toString().startsWith("5")) {
                        result = Result.retry()
                    }
                    builder.setContentText("Вигрузка завершилась")
                    NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
                    result = Result.success()
                }
            }
        })

        return result
    }


    private fun prepareFilePart(
        fileUri: Uri, body: UploadRequestBody
    ): MultipartBody.Part {
        val file = fileUri.path?.let { File(it) }
        return MultipartBody.Part.createFormData("files", file?.name, body)
    }

    override fun onProgressUpdate(percentage: Int, fileName: String) {
        createNotification(percentage, fileName)
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "photo_channel"
        const val CHANNEL_NAME = "upload_photo"
        const val WORK_NAME = "upload_photo_worker"
        private const val URI_ARRAY = "URI_ARRAY"

        fun makeRequest(uri: Array<String>) : OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<UploadWorker>().apply {
                setInputData(workDataOf(URI_ARRAY to uri))
                setConstraints(makeConstraints())
            }.build()
        }

        private fun makeConstraints() : Constraints {
            return Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        }
    }
}