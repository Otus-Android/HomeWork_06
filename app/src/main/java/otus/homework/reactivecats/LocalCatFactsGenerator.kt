package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.random.Random


class LocalCatFactsGenerator(
    private val context : Context,
) {

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact() : Single<Fact> {
        return Single.fromCallable {
            val localFacts = context.resources.getStringArray(R.array.local_cat_facts)
            val randomFactIndex = Random.nextInt(0, localFacts.size)
            Fact(localFacts[randomFactIndex])
        }
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically() : Flowable<Fact> {
        return Flowable.interval(2000, TimeUnit.MILLISECONDS, Schedulers.io())
            .map {
                val facts = context.resources.getStringArray(R.array.local_cat_facts)
                Fact(facts[Random.nextInt(5)])
            }
            .distinct()
    }
}