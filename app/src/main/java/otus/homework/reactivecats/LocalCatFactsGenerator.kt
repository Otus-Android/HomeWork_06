package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.Single
import kotlin.random.Random
import java.util.concurrent.TimeUnit

class LocalCatFactsGenerator(
    private val context: Context
) {
    private val localFacts = context.resources.getStringArray(R.array.local_cat_facts)

    private fun generateRandomFact() = Fact(text=localFacts[Random.nextInt(localFacts.size)])
    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(): Single<Fact> {
        return Single.just(generateRandomFact())
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        return Flowable
            .interval(2000, TimeUnit.MILLISECONDS)
            .map { generateRandomFact() }
            .distinctUntilChanged()
    }
}