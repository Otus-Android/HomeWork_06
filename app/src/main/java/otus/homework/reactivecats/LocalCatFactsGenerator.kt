package otus.homework.reactivecats

import android.content.Context
import io.reactivex.*
import java.util.concurrent.TimeUnit

class LocalCatFactsGenerator(
    private val context: Context
) {

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(): Flowable<Fact> {
        return Flowable.just(
            context.resources.getStringArray(R.array.local_cat_facts)
        )
            .map { Fact(it.random()) }
    }


    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        return Flowable.interval(2000, TimeUnit.MILLISECONDS)
            .flatMap { generateCatFact() }
            .distinctUntilChanged()
    }
}