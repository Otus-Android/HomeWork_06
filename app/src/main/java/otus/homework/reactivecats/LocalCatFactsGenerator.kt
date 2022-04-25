package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.Observable
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
    fun generateCatFact(): Observable<Fact> {
        return Observable.fromArray(context.resources.getStringArray(R.array.local_cat_facts))
            .map { Fact(it[Random.nextInt(0, it.size - 1)]) }
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        val factsArray = context.resources.getStringArray(R.array.local_cat_facts)
        val success = Fact(factsArray[Random.nextInt(5)])
        return Flowable.interval(2000, TimeUnit.MILLISECONDS)
            .onBackpressureBuffer(factsArray.size * 2)
            .flatMap { Flowable.fromArray(factsArray) }
            .map { Fact(it[Random.nextInt(0, it.size - 1)]) }
            .filter { it != success }
    }
}