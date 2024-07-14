package otus.homework.reactivecats

import CatImageService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.thecatapi.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val catImageService: CatImageService by lazy {
        retrofit.create(CatImageService::class.java)
    }
}
