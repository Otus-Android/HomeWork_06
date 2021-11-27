package otus.homework.reactivecats

import android.content.Context
import io.reactivex.internal.schedulers.RxThreadFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.FieldNamingPolicy

import com.google.gson.GsonBuilder

import com.google.gson.Gson
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory


class DiContainer {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://cat-fact.herokuapp.com/facts/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson()))
            .build()
    }
    fun gson(): Gson {
        return GsonBuilder()
            .create()
    }

    val service by lazy { retrofit.create(CatsService::class.java) }

    fun localCatFactsGenerator(context: Context) = LocalCatFactsGenerator(context)
}