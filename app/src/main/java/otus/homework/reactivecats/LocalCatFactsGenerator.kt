package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.Callable
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
        val stringArray = context.resources.getStringArray(R.array.local_cat_facts)
        return Single.fromCallable {
            stringArray[Random.nextInt(0, stringArray.size)]
        }.map {
            Fact(it)
        }
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        val stringArray = context.resources.getStringArray(R.array.local_cat_facts)
        return Flowable
            .interval(2000L, TimeUnit.MILLISECONDS)
            .flatMap {
                Flowable.fromCallable {
                    Fact(stringArray[Random.nextInt(0, stringArray.size)])
                }
            }
            .distinctUntilChanged()
    }
}