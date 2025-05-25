package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

class LocalCatFactsGenerator(
    private val context: Context
) {
    private var atomicFact: AtomicReference<Fact> = AtomicReference()
    private val localCatFacts = context.resources.getStringArray(R.array.local_cat_facts)

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(context: Context): Single<Fact> {
        return Single.create { emitter ->
            emitter.onSuccess(getRandomCatFact(context))
        }
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(context: Context): Flowable<Fact> =
        Flowable
            .interval(0, 2, TimeUnit.SECONDS)
            .map {
                getRandomCatFact(context)
            }.distinctUntilChanged()


    /**
    * We don't have daley after emitting the same fact anymore
     */
    fun generateCatFactPeriodicallySecondEdition(context: Context): Flowable<Fact> =
        Flowable
            .interval(0, 2, TimeUnit.SECONDS)
            .map {
                getRandomCatFactSecondEdition(context, atomicFact)
            }

    private fun getRandomCatFact(context: Context): Fact {
        return Fact(localCatFacts[Random.nextInt(localCatFacts.size)])
    }

    private fun getRandomCatFactSecondEdition(
        context: Context,
        atomicFact: AtomicReference<Fact>
    ): Fact {
        var newFact: Fact = getRandomCatFact(context)
        if (atomicFact.get() == newFact) {
            while (atomicFact.get() == newFact) {
                newFact = getRandomCatFact(context)
            }
            atomicFact.set(newFact)
        } else {
            atomicFact.set(newFact)
        }
        return newFact
    }
}