package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
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
        return Single.create { it.onSuccess(generateCatFactFromResource()) }
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        return Flowable.interval(2000, TimeUnit.MILLISECONDS)
            .concatMapSingle { generateCatFact() }
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun generateCatFactFromResource(): Fact {
        val factsValues = context.resources.getStringArray(R.array.local_cat_facts)
        val fact = Fact(factsValues[Random.nextInt(factsValues.size)])
        return fact
    }
}