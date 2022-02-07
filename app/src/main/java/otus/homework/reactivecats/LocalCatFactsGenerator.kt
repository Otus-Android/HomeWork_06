package otus.homework.reactivecats

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
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
        Log.d("LocalCatFactsGenerator", " onErrorNext")
        val fact = Fact(context.resources.getStringArray(R.array.local_cat_facts).random())
        return Single.just(fact)
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    @SuppressLint("CheckResult")
    fun generateCatFactPeriodically(): Observable<Fact> {
        return Observable.interval(2000, TimeUnit.MILLISECONDS)
            .flatMap { generateCatFact().toObservable() }
            .distinctUntilChanged()
    }
}