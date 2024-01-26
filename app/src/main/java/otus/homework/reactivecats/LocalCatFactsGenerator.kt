package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class LocalCatFactsGenerator(
    context: Context
) {

    private val random = java.util.Random()
    private val array = context.resources.getStringArray(R.array.local_cat_facts)

    private val catFactFlowable by lazy {
        Flowable.interval(0,2000, TimeUnit.MILLISECONDS)
            .map { getRandomFact() }
            .distinctUntilChanged()
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(): Single<Fact> {
        return Single.just(getRandomFact())
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        return catFactFlowable
    }

    private fun getRandomFact(): Fact {
        return Fact(getRandomString())
    }

    private fun getRandomString(): String {
        return array[random.nextInt(array.size - 1)]
    }
}
