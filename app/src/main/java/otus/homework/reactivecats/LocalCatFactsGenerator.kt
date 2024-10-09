package otus.homework.reactivecats

import android.content.Context
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
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
        val localCatFactsArray = context.resources.getStringArray(R.array.local_cat_facts)
        val result = Fact(localCatFactsArray[Random.nextInt(localCatFactsArray.size - 1)])
        return Single.just(result)
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        val localCatFactsArray = context.resources.getStringArray(R.array.local_cat_facts)

        return Flowable.create<Fact>({ emitter ->
            while (true) {
                val success = Fact(
                    context.resources.getStringArray(R.array.local_cat_facts)[Random.nextInt(
                        localCatFactsArray.size - 1
                    )]
                )
                emitter.onNext(success)
                Thread.sleep(2000)
            }
        }, BackpressureStrategy.BUFFER)
            .distinctUntilChanged()
    }
}