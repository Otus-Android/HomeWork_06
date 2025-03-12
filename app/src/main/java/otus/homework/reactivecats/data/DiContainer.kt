package otus.homework.reactivecats.data

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class DiContainer {

    companion object {
        const val BASE_URL_CATS_FACTS = "https://cat-fact.herokuapp.com/facts/"
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_CATS_FACTS)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    val service: CatsService by lazy { retrofit.create(CatsService::class.java) }
}