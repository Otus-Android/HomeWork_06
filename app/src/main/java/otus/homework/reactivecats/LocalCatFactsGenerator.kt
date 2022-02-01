package otus.homework.reactivecats

import android.annotation.SuppressLint
import android.content.Context
import io.reactivex.*
import java.util.concurrent.TimeUnit
import kotlin.math.E
import kotlin.random.Random

class LocalCatFactsGenerator(
    private val context: Context
) {
    private val fact = context.resources.getStringArray(R.array.local_cat_facts)

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */

    fun generateCatFact(): Single<Fact> {
        return Single.just(Fact(fact[Random.nextInt(fact.size)]))
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        val success = Fact(context.resources.getStringArray(R.array.local_cat_facts)[Random.nextInt(5)])

        return Flowable.interval(2000,TimeUnit.MILLISECONDS).flatMap { generateCatFact().toFlowable()}.distinctUntilChanged()
    }
}