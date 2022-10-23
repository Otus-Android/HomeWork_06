package otus.homework.reactivecats

import android.content.Context
import android.util.Log
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

private const val TAG = "LocalCatFactsGenerator"

class LocalCatFactsGenerator(
    private val context: Context
) {

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(): Single<Fact> {
        return Single.fromCallable { getRandomFactFromResource() }
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        val flowable: Flowable<Fact>
        flowable = Flowable.interval(0, Constants.PERIODIC_TIMEOUT_MS, TimeUnit.MILLISECONDS, Schedulers.io())
            .map { counter ->
            Log.d(TAG, "Generator of facts called in thread: ${Thread.currentThread().name}, count: $counter")
            getRandomFactFromResource()
        }.distinctUntilChanged().onBackpressureDrop()
        return flowable
    }

    private fun getRandomFactFromResource() = Fact(
        context.resources.getStringArray(R.array.local_cat_facts).random()
    )
}