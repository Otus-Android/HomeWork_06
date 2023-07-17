package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit.SECONDS
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
        val catFacts = context.resources.getStringArray(R.array.local_cat_facts)
        return Single.just(Fact(text = catFacts.random()))
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        val catsFact = context.resources.getStringArray(R.array.local_cat_facts)
        val success = Single.just(Fact((catsFact)[Random.nextInt(5)]))

        return Flowable
            .interval(2, SECONDS)
            .flatMapSingle { success }
            .scan { accumulator, value ->
                if (accumulator == value) {
                    Fact(catsFact.random())
                } else {
                    value
                }
            }
    }
}
