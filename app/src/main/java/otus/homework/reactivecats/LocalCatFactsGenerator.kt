package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
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
    fun generateCatFact(): Single<Fact> {
        return Single.create { e ->
            val facts = context.resources.getStringArray(R.array.local_cat_facts)
            val fact = Fact(facts[Random.nextInt(facts.size)])
            e.onSuccess(fact)
        }
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        return Flowable
            .interval(PERIOD_MS, TimeUnit.MILLISECONDS)
            .map {
                val factsArray = context.resources.getStringArray(R.array.local_cat_facts)
                Fact(factsArray[Random.nextInt(factsArray.size)])
            }
            .distinctUntilChanged()
    }

    companion object {
        const val PERIOD_MS = 2000L
    }
}