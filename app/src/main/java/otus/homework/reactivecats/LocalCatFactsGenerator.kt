package otus.homework.reactivecats

import android.content.res.Resources
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import kotlin.random.Random


class LocalCatFactsGenerator(
    private val resources: Resources
) {

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(): Single<Fact> {
        return Single.just(
            Fact(resources.getStringArray(R.array.local_cat_facts)[Random.nextInt(5)])
        )
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        var localFacts = ""
        return Flowable.interval(2000, TimeUnit.MILLISECONDS).map {
            localFacts = generateCatFactTest()
            Fact(localFacts)
        }
            .distinctUntilChanged()
    }

    private fun generateCatFactTest() =
        resources.getStringArray(R.array.local_cat_facts)[Random.nextInt(5)]
}