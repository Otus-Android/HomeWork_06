package otus.homework.reactivecats

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class DiContainer {

    private val okHttp = OkHttpClient.Builder()
        .writeTimeout(5, TimeUnit.SECONDS)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .client(okHttp)
            .baseUrl("https://catfact.ninja/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    val service: CatsService by lazy { retrofit.create(CatsService::class.java) }

    fun localCatFactsGenerator(context: Context) = LocalCatFactsGenerator(context)
}
