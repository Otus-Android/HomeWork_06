package otus.homework.reactivecats.di

import android.content.Context
import otus.homework.reactivecats.data.CatsRepositoryImpl
import otus.homework.reactivecats.data.converters.CatConverter
import otus.homework.reactivecats.data.network.CatsService
import otus.homework.reactivecats.data.storage.CatsStubStorage
import otus.homework.reactivecats.domain.CatsRepository
import otus.homework.reactivecats.utils.StringProvider
import otus.homework.reactivecats.utils.StringProviderImpl
import otus.homework.reactivecats.utils.rxjava.RxSchedulers
import otus.homework.reactivecats.utils.rxjava.RxSchedulersImpl
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Контейнер необходимых зависимостей
 *
 * @param context `application context`
 */
class DiContainer(context: Context) {

    /** Репозиторий информации о кошке */
    val repository: CatsRepository by lazy(LazyThreadSafetyMode.NONE) {
        CatsRepositoryImpl(
            provideService(),
            CatsStubStorage(stringProvider, rxSchedulers),
            CatConverter()
        )
    }

    /** Поставщик строковых значений */
    val stringProvider: StringProvider by lazy(LazyThreadSafetyMode.NONE) {
        StringProviderImpl(context)
    }

    /** Обертка получения `Scheduler`-ов */
    val rxSchedulers: RxSchedulers by lazy(LazyThreadSafetyMode.NONE) { RxSchedulersImpl() }

    private fun provideService(): CatsService = provideRetrofit().create(CatsService::class.java)

    private fun provideRetrofit() =
        Retrofit.Builder()
            .baseUrl(FACT_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()

    private companion object {
        const val FACT_BASE_URL = "https://cat-fact.herokuapp.com/facts/"
    }
}