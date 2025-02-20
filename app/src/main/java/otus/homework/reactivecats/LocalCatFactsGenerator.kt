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

    fun generateCatFact(): Single<Fact> {
        val factsArray = context.resources.getStringArray(R.array.local_cat_facts)
        val randomFact = factsArray[Random.nextInt(factsArray.size)]
        return Single.just(Fact(randomFact))
    }

    fun generateCatFactPeriodically(): Flowable<Fact> {
        val factsArray = context.resources.getStringArray(R.array.local_cat_facts)
        return Flowable.interval(2, TimeUnit.SECONDS, Schedulers.computation())
            .map { factsArray[Random.nextInt(factsArray.size)] }
            .distinctUntilChanged()
            .map { Fact(it) }
    }
}
