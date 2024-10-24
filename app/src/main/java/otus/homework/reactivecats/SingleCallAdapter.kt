package otus.homework.reactivecats

import io.reactivex.Single
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class  SingleCallAdapter<T>(private val responseType: Type): CallAdapter<T, Single<T>> {
    override fun responseType(): Type = responseType


    override fun adapt(call: Call<T>): Single<T> =
        Single.create { emitter ->
            call.enqueue(object: Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    response.body()?.let { emitter.onSuccess(it) }
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    call.cancel()
                    if (!emitter.isDisposed) emitter.onError(t)
                }
            })
        }
}

class SingleCallAdapterFactory private constructor(): CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        try {
            val enclosingType = returnType as ParameterizedType

            if (getRawType(enclosingType) != Single::class.java) {
                return null
            }

            val type = enclosingType.actualTypeArguments[0]

            return SingleCallAdapter<ParameterizedType>(type)
        } catch (e: ClassCastException) {
            return null
        }
    }

    companion object {
        fun create(): SingleCallAdapterFactory = SingleCallAdapterFactory()
    }
}

