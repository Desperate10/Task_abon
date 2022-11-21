package ua.POE.Task_abon.network

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
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

@HiltWorker
class UploadWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val ioDispatcher: CoroutineDispatcher
) : CoroutineWorker(context, workerParameters), UploadRequestBody.UploadCallback {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var uploadProgress = 0
    private var photoName = ""

    /*override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            NOTIFICATION_ID, createNotification()
        )
    }*/

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val channel =
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
    }

    private suspend fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
        setForeground(
            ForegroundInfo(
                NOTIFICATION_ID,
                NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(photoName)
                    .setContentText("Вигрузка фото...")
                    .setProgress(100, uploadProgress, false)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setOngoing(true)
                    .setAutoCancel(true)
                    .build()
            )
        )
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(photoName)
            .setContentText("Вигрузка фото...")
            .setProgress(100, uploadProgress, false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setAutoCancel(true)
            .build()
        return notification
    }


    override suspend fun doWork(): Result  {
        startForegroundService()
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
                uploadProgress = 0
                result = Result.failure()
            }

            override fun onResponse(
                call: Call<UploadResponse>, response: Response<UploadResponse>
            ) {
                response.body()?.let {
                    if (response.code().toString().startsWith("5")) {
                        result = Result.retry()
                    }
                    uploadProgress = 100
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
        uploadProgress = percentage
        photoName = fileName
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "photo_channel"
        const val CHANNEL_NAME = "upload_photo"
        const val WORK_NAME = "upload_photo_worker"
        const val ERROR_MSG = "error_message"
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