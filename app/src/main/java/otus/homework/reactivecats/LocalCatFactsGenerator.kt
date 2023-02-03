package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
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
        val facts = context.resources.getStringArray(R.array.local_cat_facts)
        val fact = facts[Random.nextInt(facts.size)]
        return Single.just(Fact(fact))
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Observable<Fact> {
        val facts = context.resources.getStringArray(R.array.local_cat_facts)
        return BehaviorSubject.generate<Fact?> {
            val fact = facts[Random.nextInt(facts.size)]
            it.onNext(Fact(fact))
            Thread.sleep(2000)
        }
            .distinctUntilChanged()
    }
}