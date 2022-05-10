package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class LocalCatFactsGenerator(
    private val context: Context
) {

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(): Single<Fact> {
        return Single.create<Fact> { singleEmitter ->
            try {
                val items = context.resources.getStringArray(R.array.local_cat_facts)
                val randomItem = items.random()
                val factItem = Fact(text = randomItem)

                singleEmitter.onSuccess(factItem)
            } catch (throwable: Throwable) {
                singleEmitter.onError(throwable)
            }
        }.subscribeOn(Schedulers.computation())
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        val repeatTime = 2L
        val timeUnit = TimeUnit.SECONDS
        val newFlowableCatsFact = generateCatFact().toFlowable()

        return Flowable.interval(repeatTime, timeUnit)
            .flatMap { newFlowableCatsFact }
            .distinctUntilChanged()
    }
}