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
    fun generateCatFact(): Single<Fact> {
        return Observable
            .fromArray(context.resources.getStringArray(R.array.local_cat_facts))
            .map { facts -> Fact(facts[Random.nextInt()]) }
            .singleOrError()
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        var safeSuccess: Fact? = null

        return Observable.zip(
            Observable.fromCallable { Fact(context.resources.getStringArray(R.array.local_cat_facts)[Random.nextInt(5)]) },
            Observable.interval(2000, TimeUnit.MILLISECONDS)
        ) { fact, _ -> fact }
            .doOnNext { if (safeSuccess == it) return@doOnNext }
            .doAfterNext { safeSuccess = it }
            .toFlowable(BackpressureStrategy.BUFFER)
    }
}
