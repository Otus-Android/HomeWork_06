package otus.homework.reactivecats

import android.content.Context
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
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
    fun generateCatFact(): Single<List<Fact>> {
        val randomFact = context.resources.getStringArray(R.array.local_cat_facts).random()
        return Single.just(listOf(Fact(randomFact)))
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<List<Fact>> {
        return Flowable.interval(0, 2000, TimeUnit.MILLISECONDS)
            .flatMap {
                Flowable.create<List<Fact>>({ emitter ->
                    val fact = Fact(context.resources.getStringArray(R.array.local_cat_facts)[Random.nextInt(5)])
                    emitter.onNext(listOf(fact))
                    emitter.onComplete()
                }, BackpressureStrategy.BUFFER)
            }
            .distinctUntilChanged()
    }
}
