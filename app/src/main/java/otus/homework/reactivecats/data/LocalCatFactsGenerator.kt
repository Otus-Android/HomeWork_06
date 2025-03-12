package otus.homework.reactivecats.data

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.Single
import otus.homework.reactivecats.R
import java.util.concurrent.TimeUnit

class LocalCatFactsGenerator(
    private val context: Context
) {

    companion object {
        const val UPDATING_PERIOD_SECONDS: Long = 2000L
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(): Single<Fact> {
        val localFacts = context.resources.getStringArray(R.array.local_cat_facts)
        val fact = Fact(localFacts.random())
        return Single.just(fact)
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        val localFacts = context.resources.getStringArray(R.array.local_cat_facts)
        return Flowable.fromArray(localFacts)
            .map { Fact(localFacts.random()) }
            .repeatWhen { it.delay(UPDATING_PERIOD_SECONDS, TimeUnit.SECONDS) }
            .distinctUntilChanged()
    }
}