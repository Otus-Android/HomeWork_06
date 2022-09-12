package otus.homework.reactivecats

import android.content.Context
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.random.nextInt

class LocalCatFactsGenerator(
    private val context: Context
) {

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(): Single<Fact> =
        context.resources.getStringArray(R.array.local_cat_facts).let { facts ->
            Single.fromCallable {
                Fact(
                    text = facts[Random.nextInt(facts.indices)]
                )
            }
        }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> =
        context.resources.getStringArray(R.array.local_cat_facts).let { facts ->
            Flowable
                .generate<Fact?> { emitter ->
                    emitter.onNext(Fact(facts[Random.nextInt(facts.indices)]))
                }
                .distinctUntilChanged()
                .zipWith(
                    Flowable.interval(CAT_GENERATION_PERIOD_MILLISECONDS, TimeUnit.MILLISECONDS)
                ) { item, _ -> item }
        }

    companion object {
        private const val CAT_GENERATION_PERIOD_MILLISECONDS = 2000L
    }


}