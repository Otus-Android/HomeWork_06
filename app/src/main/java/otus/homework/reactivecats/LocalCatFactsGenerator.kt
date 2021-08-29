package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import kotlin.random.Random

private const val DELAY = 2000L

class LocalCatFactsGenerator(
    private val context: Context
) {

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(): Single<ActivityResponse> {
        val array = context.resources.getStringArray(R.array.local_cat_facts)
        return Single.just(ActivityResponse(array[Random.nextInt(array.size)]))
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<ActivityResponse> {
        val array = context.resources.getStringArray(R.array.local_cat_facts)
        return Flowable.interval(DELAY, TimeUnit.MILLISECONDS)
            .map { ActivityResponse(array[Random.nextInt(array.size)]) }
            .distinctUntilChanged()
    }
}