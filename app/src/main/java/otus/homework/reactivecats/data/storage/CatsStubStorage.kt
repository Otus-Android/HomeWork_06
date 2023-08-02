package otus.homework.reactivecats.data.storage

import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import otus.homework.reactivecats.R
import otus.homework.reactivecats.data.models.Fact
import otus.homework.reactivecats.utils.StringProvider
import otus.homework.reactivecats.utils.rxjava.RxSchedulers
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Реализация локального хранилища информации о коте, использующая
 * заглушечные сохраненные данные
 *
 * @param provider поставщик строковых значений
 * @param schedulers обертка получения `Scheduler`-ов
 */
class CatsStubStorage(
    private val provider: StringProvider,
    private val schedulers: RxSchedulers
) : CatsStorage {

    private val facts: Array<String> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        provider.getStringArray(R.array.local_cat_facts)
    }

    override fun getRandomCatFact(): Single<Fact> = generateCatFact()

    // Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
    // чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
    // обернутую в подходящий стрим(Flowable/Single/Observable и т.п)

    /** Получить информацию о случайном коте [Fact] в виде [Single] */
    private fun generateCatFact(): Single<Fact> =
        Single.fromCallable { facts }
            .map { facts -> Fact(text = facts.getRandom()) }
            .subscribeOn(schedulers.io)

    // Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
    // чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
    // Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.

    /** Получить периодически обновляемый факт о коте [Fact] в виде [Flowable] */
    fun generateCatFactPeriodically(): Flowable<Fact> =
        Flowable.interval(INTERVAL, TimeUnit.MILLISECONDS)
            .flatMapSingle { generateCatFact() }

    private fun Array<String>.getRandom() = this[Random.nextInt(size)]

    private companion object {
        const val INTERVAL = 2000L
    }
}