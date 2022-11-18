package ua.POE.Task_abon.network

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
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
import ua.POE.Task_abon.presentation.task.TaskViewModel.Companion.KEY_IMAGE_URI
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

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            NOTIFICATION_ID, createNotification()
        )
    }

    private fun createNotification() : Notification {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Загрузка фото")
            .setContentText("Загрузка продовжується")
            .setProgress(100, 5, false)
            .setOngoing(true)
            .build()
        return notification
    }

    override suspend fun doWork(): Result =
        withContext(ioDispatcher) {
            val inputPhotoUri = inputData.getStringArray(KEY_IMAGE_URI)
            return@withContext try{
                uploadImages(inputPhotoUri?.toList() ?: emptyList())
            } catch (e: IOException) {
                Result.failure()
            } catch (e: Exception) {
                Result.failure()
            }
        }


    private fun uploadImages(uriStrings: List<String>) : Result {

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
               result = Result.failure()
            }

            override fun onResponse(
                call: Call<UploadResponse>, response: Response<UploadResponse>
            ) {
                response.body()?.let {
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

    override fun onProgressUpdate(percentage: Int) {

    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "photo_channel"
    }
}