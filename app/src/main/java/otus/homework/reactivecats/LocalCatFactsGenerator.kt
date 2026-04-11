package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class LocalCatFactsGenerator(
    private val context: Context
) {

    private val localFactsArray by lazy { context.resources.getStringArray(R.array.local_cat_facts) }
    private val localFactsSize by lazy { localFactsArray.size }

    /**
     * Реализуйте функцию [otus.homework.reactivecats.LocalCatFactsGenerator.generateCatFact] так,
     * чтобы она возвращала Fact со случайной строкой из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    private fun generateCatFact() = Single.fromCallable {
        Fact(localFactsArray[Random.nextInt(localFactsSize)])
    }

    /**
     * Реализуйте функцию [otus.homework.reactivecats.LocalCatFactsGenerator.generateCatFactPeriodically] так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> =
        Flowable.interval(2000, TimeUnit.MILLISECONDS)
            .flatMap { generateCatFact().toFlowable() }
            .distinctUntilChanged()
}