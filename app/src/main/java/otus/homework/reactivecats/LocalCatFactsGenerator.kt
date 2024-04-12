package otus.homework.reactivecats

import android.content.Context
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
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
        return Observable.create { getRandomFact() }
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        return Flowable.create<Fact>(
            /* source = */ { emitter ->
                while (emitter.isCancelled.not()) {
                    emitter.onNext(getRandomFact())
                    Thread.sleep(2000)
                }
            },
            /* mode = */ BackpressureStrategy.LATEST
        )
            .distinctUntilChanged()
    }

    private fun getRandomFact(): Fact =
        Fact(context.resources.getStringArray(R.array.local_cat_facts)[Random.nextInt(5)])
}