package otus.homework.reactivecats

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import otus.homework.reactivecats.callAdapter.CatsCallAdapterFactory
import retrofit2.Retrofit

class DiContainer {

    private val retrofit by lazy {
        val type = MediaType.get("application/json")
        Retrofit.Builder()
            .baseUrl("https://catfact.ninja/")
            .addConverterFactory(provideJson().asConverterFactory(type))
            .addCallAdapterFactory(CatsCallAdapterFactory.create())
            .build()
    }

    private fun provideJson(): Json {
        return Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }

    val service by lazy { retrofit.create(CatsService::class.java) }

    fun localCatFactsGenerator(context: Context) = LocalCatFactsGenerator(context)
}