package otus.homework.reactivecats

import android.content.Context
import io.reactivex.rxjava3.core.Flowable
import java.util.concurrent.TimeUnit

class LocalCatFactsGenerator(
    private val context: Context
) {
//
//    /**
//     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
//     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
//     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
//     */
    private val factMessages = context.resources.getStringArray(R.array.local_cat_facts)

    fun generateCatFact(): Flowable<Fact> {
        val rand = (0 until factMessages.size).random()
        System.out.println("generateCatFact ${Fact(factMessages[rand])}")
        return Flowable.just(Fact(factMessages[rand]))}
//
//    /**
//     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
//     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
//     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
//     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
    System.out.println("generateCatFactPeriodically")
        return  Flowable.interval(2000, TimeUnit.MILLISECONDS)
                        .flatMap { generateCatFact()}
                        .distinctUntilChanged()
    }
}