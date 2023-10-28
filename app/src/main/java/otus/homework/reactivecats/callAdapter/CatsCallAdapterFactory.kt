package otus.homework.reactivecats.callAdapter

import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.lang.reflect.Type

class CatsCallAdapterFactory private constructor(): CallAdapter.Factory() {

    private val mainFactory = RxJava2CallAdapterFactory.create()

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        val defaultAdapter = mainFactory.get(returnType, annotations, retrofit) ?: return null
        return CatsCallAdapter(defaultAdapter)
    }

    companion object {
        fun create(): CallAdapter.Factory = CatsCallAdapterFactory()
    }
}