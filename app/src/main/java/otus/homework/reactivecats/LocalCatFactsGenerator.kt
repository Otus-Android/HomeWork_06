package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
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
        return Single.fromCallable {
            // Получаем массив строк из ресурсов
            val factsArray = context.resources.getStringArray(R.array.local_cat_facts)

            // Проверяем, что массив не пустой
            if (factsArray.isEmpty()) {
                throw IllegalStateException("Local cat facts array is empty")
            }

            // Выбираем случайную строку
            val randomIndex = Random.nextInt(factsArray.size)
            val randomFactText = factsArray[randomIndex]

            // Создаем объект Fact
            Fact(
                fact = randomFactText
            )
        }
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
//    fun generateCatFactPeriodically(): Flowable<Fact> {
//        val success = Fact(context.resources.getStringArray(R.array.local_cat_facts)[Random.nextInt(5)])
//        return Flowable.empty()
//    }

    fun generateCatFactPeriodically(): Flowable<Fact> {
        return Flowable.interval(2000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .map {
                val array = context.resources.getStringArray(R.array.local_cat_facts)
                val text = array[Random.nextInt(array.size)]
                Fact(text)
            }
            .distinctUntilChanged { old, new -> old.fact == new.fact }
    }
}