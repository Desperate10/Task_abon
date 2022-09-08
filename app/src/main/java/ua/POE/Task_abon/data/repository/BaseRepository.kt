package ua.POE.Task_abon.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.POE.Task_abon.utils.Resource
import java.io.FileNotFoundException


abstract class BaseRepository {

    suspend fun <T> saveReadFile(
        readFile: suspend () -> T
    ) : Resource<T> {
        return withContext(Dispatchers.IO) {
            try {
                Resource.success(readFile.invoke())
            } catch (throwable: Throwable) {
                when(throwable) {
                    is FileNotFoundException ->
                        Resource.error("Файл не найден")
                    else -> Resource.error("Ошибка чтения файла")
                }
            }
        }
    }
}