package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.random.nextInt

class LocalCatFactsGenerator(
    private val context: Context
) {

    private companion object {
        const val INTERVAL = 2000L
    }
    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(): Single<Fact> {
        return Single.create { e ->
            e.onSuccess(
                Fact(context.resources.getStringArray(R.array.local_cat_facts)[Random.nextInt(0..5)])
            )
        }
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */

    fun generateCatFactPeriodically(): Flowable<Fact> {
        return Flowable
            .interval(INTERVAL, TimeUnit.MILLISECONDS)
            .map { Fact(context.resources.getStringArray(R.array.local_cat_facts)[Random.nextInt(5)]) }
            .distinctUntilChanged()
    }
}
