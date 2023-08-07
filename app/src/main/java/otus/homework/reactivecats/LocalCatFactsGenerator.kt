package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import kotlin.random.Random.Default.nextInt


class LocalCatFactsGenerator(
    private val context: Context
) {

    private val catsFactsArray = context.resources.getStringArray(R.array.local_cat_facts)
    private val arraySize = catsFactsArray.size
    private val interval: Long = 2000

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(): Single<Fact> {
        return Single.just(Fact((catsFactsArray)[nextInt(arraySize)]))
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        val success = Fact(((catsFactsArray)[nextInt(arraySize)]))
        return Flowable
            .timer(interval, TimeUnit.MILLISECONDS)
            .repeat()
            .map { success }
            .distinctUntilChanged()
    }
}
