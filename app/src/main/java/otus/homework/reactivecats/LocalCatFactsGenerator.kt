package otus.homework.reactivecats

import android.content.Context
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
        return Single.create {
            val fact = Fact(context.resources.getStringArray(R.array.local_cat_facts)[Random.nextInt(5)])
            it.onSuccess(fact)
        }
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Observable<Fact> {
        val source = Observable.interval(2, TimeUnit.SECONDS)
            .map {
                val fact = Fact(context.resources.getStringArray(R.array.local_cat_facts)[Random.nextInt(5)])
                fact
            }
           .distinctUntilChanged()

        return source
    }
}