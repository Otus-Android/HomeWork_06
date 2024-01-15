package otus.homework.reactivecats

import android.content.Context
import android.os.SystemClock.sleep
import android.util.Log
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.Executors.newSingleThreadExecutor
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
        val success = Fact(context.resources.getStringArray(R.array.local_cat_facts)[Random.nextInt(5)])
        return Single.create { it.onSuccess(success) }
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        return Flowable.interval(1, TimeUnit.SECONDS).map {
            Fact(context.resources.getStringArray(R.array.local_cat_facts)[Random.nextInt(5)])
        }
    }
}