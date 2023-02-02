package otus.homework.reactivecats

import android.content.Context
import io.reactivex.rxjava3.core.Single
import kotlin.random.Random

class LocalCatFactsGenerator(
    private val context: Context
) {
//
//    /**
//     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
//     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
//     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
//     */
    private val myArray = context.resources.getStringArray(R.array.local_cat_facts)

    fun generateCatFact(): Single<Fact> {
        val rand = (0 until myArray.size).random()
        return Single.just(Fact(myArray[rand]))}
//
//    /**
//     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
//     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
//     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
//     */
//    fun generateCatFactPeriodically(): Flowable<Fact> {
//        val success = Fact(context.resources.getStringArray(R.array.local_cat_facts)[Random.nextInt(5)])
//        return Flowable.empty()
//    }
}