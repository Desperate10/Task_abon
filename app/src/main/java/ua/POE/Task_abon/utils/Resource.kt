package ua.POE.Task_abon.utils

sealed class Resource<out T>(val data: T?, val message: String? = null) {
    class Success<T>(data: T?) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(val isLoading: Boolean = true, data: T? = null): Resource<T>(null)
}

