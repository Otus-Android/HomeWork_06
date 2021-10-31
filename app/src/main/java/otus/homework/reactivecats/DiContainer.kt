package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import otus.homework.reactivecats.network.CatsService
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class DiContainer {

	private val retrofit by lazy {
		val client = OkHttpClient.Builder().addNetworkInterceptor(
			HttpLoggingInterceptor().apply { HttpLoggingInterceptor.Level.BODY }
		)

		Retrofit.Builder()
			.client(client.build())
			.baseUrl("https://cat-fact.herokuapp.com/facts/")
			.addConverterFactory(GsonConverterFactory.create())
			.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
			.build()
	}

	val service by lazy { retrofit.create(CatsService::class.java) }

	val productionSchedulers by lazy {
		object : ISchedulers {
			override fun io(): Scheduler = Schedulers.io()
			override fun ui(): Scheduler = AndroidSchedulers.mainThread()
		}
	}

	fun localCatFactsGenerator(context: Context) = LocalCatFactsGenerator(context)
}