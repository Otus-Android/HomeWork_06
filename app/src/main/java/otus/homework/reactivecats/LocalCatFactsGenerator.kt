package otus.homework.reactivecats

import android.content.Context
import io.reactivex.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class LocalCatFactsGenerator(
    private val context: Context
) {

    private val localCatArray = context.resources.getStringArray(R.array.local_cat_facts)

    private fun getRandomFact(): Fact {
        val rnd = Random.nextInt(localCatArray.size)
        return Fact(localCatArray[rnd])
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(): Single<Fact>{
        return Single.create { emitter -> emitter.onSuccess(getRandomFact()) }
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        return Flowable.create(
            FlowableOnSubscribe<Fact> { emitter -> emitter.onNext(getRandomFact()) },
            BackpressureStrategy.BUFFER)
            .delay(2000, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
    }
}