package otus.homework.reactivecats

import android.content.Context
import android.util.Log
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import java.lang.Thread.sleep
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
        Log.d("flow generateCatFact()", "After Error")
        return Single.just(
            Fact(
                context.resources.getStringArray(R.array.local_cat_facts)[Random.nextInt(
                    5
                )]
            )
        )
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {

        val source: Flowable<Fact> = Flowable.create<Fact?>(
            { emitter ->
                repeat(10) {
                    val success =
                        Fact(
                            context.resources.getStringArray(R.array.local_cat_facts)[Random.nextInt(
                                5
                            )]
                        )
                    Log.d("flow", "$it $success")
                    emitter.onNext(success)
                    sleep(2000)
                }
                emitter.onComplete()
            }, BackpressureStrategy.BUFFER

        ).distinctUntilChanged()


        return source
    }
}