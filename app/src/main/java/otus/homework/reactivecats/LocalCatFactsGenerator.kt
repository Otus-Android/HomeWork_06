package otus.homework.reactivecats

import android.content.Context
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.concurrent.TimeUnit

class LocalCatFactsGenerator(
    private val context: Context
) {

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(): Single<Fact> {
        val arrayFacts = context.resources.getStringArray(R.array.local_cat_facts)
        val randomIndex = (arrayFacts.indices).random()
        return Single.just(Fact(arrayFacts[randomIndex]))
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        val arrayFacts = context.resources.getStringArray(R.array.local_cat_facts)

        return Flowable.interval(PERIOD_EMIT, TimeUnit.MILLISECONDS)
            .map { (arrayFacts.indices).random() }
            .distinctUntilChanged()
            .flatMap { randomIndex ->
                Flowable.create({
                    val success = arrayFacts[randomIndex]
                    it.onNext(Fact(success))
                }, BackpressureStrategy.DROP)
            }
    }

}