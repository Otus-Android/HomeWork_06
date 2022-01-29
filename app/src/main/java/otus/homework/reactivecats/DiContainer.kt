package otus.homework.reactivecats

import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory

class DiContainer {
    private  val BASE_URL = "https://cat-fact.herokuapp.com/facts/"
    private  val CONNECT_TIMEOUT_SEC = 5L
    private  val READ_TIMEOUT_SEC = 5L
    private  val WRITE_TIMEOUT_SEC = 5L





    private val retrofit by lazy {


        Retrofit.Builder()

            .baseUrl(this.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())

            .build()
    }

    val service by lazy { retrofit.create(CatsService::class.java) }

    fun localCatFactsGenerator(context: Context) = LocalCatFactsGenerator(context)
}