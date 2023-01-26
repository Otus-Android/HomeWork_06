package otus.homework.reactivecats

import io.reactivex.Flowable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class LocalCatFactsGenerator(
    private val resourceRepository: ResourceRepository
) {

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(resId: Int): Single<Fact> {
        return Single.fromCallable { generateFactFromResources(resId) }
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(resId: Int): Flowable<Fact> {
        return Flowable.fromCallable { generateFactFromResources(resId) }
            .delay(2000L, TimeUnit.MILLISECONDS)
            .repeat()
            .distinctUntilChanged()
    }

    private fun generateFactFromResources(resId: Int): Fact {
        val index = Random.nextInt(5)
        val text = resourceRepository.getStringArray(resId)[index]
        return Fact(text)
    }
}