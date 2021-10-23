package otus.homework.reactivecats

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class DiContainer {

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://cat-fact.herokuapp.com/facts/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(createHttpClient())
            .build()
    }
    
    private fun createHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
        .build()
    
    val service by lazy { retrofit.create(CatsService::class.java) }

    fun localCatFactsGenerator(context: Context) = LocalCatFactsGenerator(context)
}