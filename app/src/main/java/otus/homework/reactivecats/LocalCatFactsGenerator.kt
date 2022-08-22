package otus.homework.reactivecats

import android.content.Context
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableOnSubscribe
import io.reactivex.Single
import kotlin.random.Random

class LocalCatFactsGenerator(
    private val context: Context
) {

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(): Single<Fact> {
        val catFactsArray = context.resources.getStringArray(R.array.local_cat_facts)
        val randomFact = catFactsArray[Random.nextInt(catFactsArray.size)]
        return Single.just(Fact(randomFact))
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        val catFactsArray = context.resources.getStringArray(R.array.local_cat_facts)
        var lastEmittedString = ""
        val flowable = Flowable.create(
            FlowableOnSubscribe<Fact> { emitter ->
                val randomFact = catFactsArray[Random.nextInt(catFactsArray.size)]
                if (randomFact != lastEmittedString) {
                    emitter.onNext(Fact(randomFact))
                    lastEmittedString = randomFact
                }
                Thread.sleep(2000L)
            }, BackpressureStrategy.LATEST
        )
        return flowable
    }
}