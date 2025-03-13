package otus.homework.reactivecats.data

import android.util.Log
import retrofit2.Response

abstract class BaseRepository {

    protected val netService = DiContainer().service

    protected fun <T : Any> apiResultHandler(
        response: Response<T>,
    ): ApiResult<T> {
        try {
            return when {
                response.isSuccessful -> {
                    response.body()?.let {
                        Log.e(null, "[BaseRepository]: Success request")
                        ApiResult.Success(it)
                    } ?: ApiResult.Error("Success, no body received")
                }
                else -> {
                    Log.e(null, "[BaseRepository]: Error request")
                    ApiErrorFactory().create(response)
                }
            }
        } catch (e: Exception) {
            Log.e(null, "[BaseRepository]: Failed request, server error")
            return ApiResult.ServerError
        }
    }

    interface IErrorFactory {
        fun <T : Any> create(response: Response<T>): ApiResult<T>
    }

    inner class ApiErrorFactory : IErrorFactory {

        override fun <T : Any> create(response: Response<T>): ApiResult<T> {
            return when (response.code()) {
                in 300..526 ->
                    ApiResult.Error(response.message())

                else -> ApiResult.ServerError
            }
        }
    }

    sealed class ApiResult<out T : Any> {
        data class Success<out T : Any>(val data: T) : ApiResult<T>()
        data class Error(val message: String?) : ApiResult<Nothing>()
        data object ServerError : ApiResult<Nothing>()
    }
}