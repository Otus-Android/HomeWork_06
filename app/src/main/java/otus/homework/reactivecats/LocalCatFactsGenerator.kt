package otus.homework.reactivecats

import android.content.Context
import io.reactivex.*
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
        val randomString = context.resources.getStringArray(R.array.local_cat_facts).random()
        return Single.just(Fact(randomString))
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        return Flowable.create<Fact?>({ emitter: FlowableEmitter<Fact?> ->
            var lastFact = Fact("")

            while (true) {
                val success = Fact(context.resources.getStringArray(R.array.local_cat_facts)[Random.nextInt(6)])
                if (lastFact != success)
                    emitter.onNext(success)
                else
                    continue
                lastFact = success
                sleep(2000)
            }
        }, BackpressureStrategy.BUFFER)
    }
}