package otus.homework.reactivecats

import android.content.Context
import io.reactivex.*
import java.util.concurrent.Flow
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
        val randomFact = Fact(context.resources.getStringArray(R.array.local_cat_facts)[Random.nextInt(5)])
        return Single.just(randomFact)
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Observable<Fact> {
        return Observable.interval(2000, TimeUnit.MILLISECONDS)
            .flatMap { Observable.create<Fact> { emitter ->
                    emitter.onNext(
                        Fact(context.resources.getStringArray(R.array.local_cat_facts)[Random.nextInt(5)])
                    )
                }
            }
            .distinctUntilChanged()
    }
}