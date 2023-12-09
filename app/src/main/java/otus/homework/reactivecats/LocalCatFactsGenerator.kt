package otus.homework.reactivecats

import android.content.Context
import io.reactivex.BackpressureStrategy
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
    private fun generateCatFact(): Single<Fact> {
        return Single.create { singleEmitter ->
            try {
                val localCatFacts = context.resources.getStringArray(R.array.local_cat_facts)
                val randomIndex = Random.nextInt(localCatFacts.size - 1)
                val fact = Fact(text = localCatFacts[randomIndex])
                singleEmitter.onSuccess(fact)
            } catch (e: Exception) {
                singleEmitter.onError(e)
            }
        }
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        val factObservable = generateCatFact().toFlowable().distinctUntilChanged()
        return Flowable.interval(DELAY_SECOND, TimeUnit.SECONDS)
            .flatMap { factObservable }
    }

    companion object {
        const val DELAY_SECOND = 2L
    }
}