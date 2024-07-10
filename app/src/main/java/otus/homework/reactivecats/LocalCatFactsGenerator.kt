package otus.homework.reactivecats

import android.content.Context
import android.util.Log
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class LocalCatFactsGenerator(
    private val context: Context
) {

    private val tag = javaClass.simpleName
    private val localCatFacts = context.resources.getStringArray(R.array.local_cat_facts)

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(): Single<Fact> {
        return Single.fromCallable {
            getRandomLocalCatFact()
        }
    }

    private fun getRandomLocalCatFact(): Fact =
        Fact(localCatFacts[Random.Default.nextInt(localCatFacts.size)])

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        return Flowable.interval(
            LOCAL_CAT_FACTS_INTERVAL_MS,
            TimeUnit.MILLISECONDS,
            Schedulers.computation()
        )
            .flatMap {
                Flowable.fromCallable { getRandomLocalCatFact() }
                    .doOnError { error ->
                        Log.e(tag, "Error generating cat fact: ${error.message}")
                    }
                    .onErrorResumeNext(
                        Flowable.just(Fact(context.getString(R.string.default_error_text)))
                    )
            }
            .distinctUntilChanged()
    }

    companion object {
        private const val LOCAL_CAT_FACTS_INTERVAL_MS = 2000L
    }
}