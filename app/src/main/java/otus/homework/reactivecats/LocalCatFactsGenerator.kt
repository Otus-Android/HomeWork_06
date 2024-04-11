package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.lang.Exception
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class LocalCatFactsGenerator(
    private val context: Context
) {

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(): Observable<Fact> {
        val factsArray = context.resources.getStringArray(R.array.local_cat_facts)
        val fact = Fact(factsArray[factsArray.indices.random()])
        return Observable.just(fact)
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        val factsArray = context.resources.getStringArray(R.array.local_cat_facts)
        var previousFact: Fact? = null

        val flowable = Flowable.interval(0, 2, TimeUnit.SECONDS, Schedulers.io())
            .map {
                val randomFact = factsArray.indices.random()
                Fact(factsArray[randomFact])
            }.filter { fact ->
                if (fact != previousFact) {
                    previousFact = fact
                    true
                } else false
            }

        return flowable
    }
}