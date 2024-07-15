package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class LocalCatFactsGenerator(
    private val context: Context
) {
    val facts: Array<String> by lazy {
        context.resources.getStringArray(R.array.local_cat_facts)
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(): Single<Fact> {
        /**
         * Тут можно было бы сделать так:
         * randomText = facts.toList().shuffled().first()
         * (а лучше toList() у самой переменной facts)
         * Это было бы безопаснее, так как страхует от случайного вылезания за границу массива,
         * но создает промежуточную коллекцию
         */
        val randomNumber = Random.nextInt(from = 0, until = facts.size)
        val randomFact = Fact(facts[randomNumber])
        return Single.just(randomFact)
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Observable<Fact> {
        return Observable.interval(
            FACTS_INITIAL_REQUEST_DELAY,
            FACTS_PERIOD_REQUEST_DELAY,
            TimeUnit.SECONDS,
            /**
             * Поставил trampoline, так как, фактически, не важно,
             * на каком потоке выполняется данная операция (она не ресурсоемкая).
             * Прикладывать силы на переключение потока тут будет лишним
             */
            Schedulers.trampoline()
        )
            .flatMap { generateCatFact().toObservable() }
            .distinctUntilChanged()
    }
}