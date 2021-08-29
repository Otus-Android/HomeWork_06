package otus.homework.reactivecats

import android.content.Context
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class DiContainer {

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.boredapi.com")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val service by lazy { retrofit.create(ActivitiesService::class.java) }
    val mainScheduler = AndroidSchedulers.mainThread()
    val ioScheduler = Schedulers.io()

    fun localCatFactsGenerator(context: Context) = LocalCatFactsGenerator(context)
}