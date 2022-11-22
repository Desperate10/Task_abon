package ua.POE.Task_abon.network


import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import ua.POE.Task_abon.BuildConfig

interface MyApi {
    @Multipart
    @POST("upload")
    fun uploadImage(
        @Part images: List<MultipartBody.Part>,
        @Part("files") desc: RequestBody
    ): Call<UploadResponse>

    companion object{
        operator fun invoke() :MyApi{

            return Retrofit.Builder()
                .baseUrl(BuildConfig.MY_CLOUD_API)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MyApi::class.java)
        }
    }
}