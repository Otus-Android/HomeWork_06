package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class LocalCatFactsGenerator(
    private val context: Context
) {

    private val catFactsSubject = BehaviorSubject
        .generate { emitter ->
            val catFacts = context.resources.getStringArray(R.array.local_cat_facts)
            emitter.onNext(catFacts)
        }
        .subscribeOn(Schedulers.io())

    private val catFactsSingle = catFactsSubject.firstOrError()

    private fun getRandomFact(catFacts: Array<String>): String {
        return catFacts[Random.nextInt(catFacts.size)]
    }

    private fun mapToFact(catFact: String): Fact {
        return Fact(catFact)
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(): Single<Fact> = catFactsSingle
        .map(::getRandomFact)
        .map(::mapToFact)

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> =
        Flowable.interval(0, 2000, TimeUnit.MILLISECONDS)
            .switchMapSingle { generateCatFact() }
            .buffer(2)
            .filter { facts ->
                val (previousFact, currentFact) = facts.zipWithNext().first()
                currentFact != previousFact
            }
            .map { it.component2() }

}