package otus.homework.reactivecats

import android.content.Context
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import java.lang.Thread.sleep
import kotlin.random.Random

class LocalCatFactsGenerator(private val context: Context) {

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(): Single<Fact> {
        return Single.fromCallable {
            Fact(context.resources.getStringArray(R.array.local_cat_facts)[Random.nextInt(5)])
        }
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        return Flowable.create(
            { emitter ->
                var previous = ""
                while (true) {
                    val success =
                        Fact(
                            context.resources.getStringArray(R.array.local_cat_facts)[Random.nextInt(
                                5
                            )]
                        )
                    if (success.text != previous) {
                        previous = success.text
                        emitter.onNext(success)
                    }
                    sleep(2000)
                }
            }, BackpressureStrategy.BUFFER
        )
    }
}
