package otus.homework.reactivecats

import android.content.Context
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.concurrent.TimeUnit

class LocalCatFactsGenerator(
    private val context: Context
) {
    private val facts = context.resources.getStringArray(R.array.local_cat_facts)

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(): Single<Fact> = Single.just(getRandomFact())

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        var previousFact = Fact("")
        return Flowable.create<Fact>(
            { emitter ->
                getRandomFact().let {
                    if (it != previousFact) {
                        previousFact = it
                        emitter.onNext(it)
                    }
                }
                emitter.onComplete()
            },
            BackpressureStrategy.LATEST
        ).delay(2, TimeUnit.SECONDS).repeat()
    }


    private fun getRandomFact(): Fact = Fact(text = facts[(0..facts.lastIndex).random()])
}