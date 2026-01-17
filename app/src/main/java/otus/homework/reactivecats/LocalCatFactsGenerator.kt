package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class LocalCatFactsGenerator(
    private val context: Context
) {

    fun generateCatFact(): Single<Fact> {
        return Single.just(newCatFact())
    }

    fun generateCatFactPeriodically(): Flowable<Fact> {
        return Flowable.interval(2000, TimeUnit.MILLISECONDS)
            .map {
                newCatFact()
            }
            .distinctUntilChanged { oldFact, newFact ->
                oldFact.fact == newFact.fact
            }
    }

    private fun newCatFact(): Fact {
        val factsArray = context.resources.getStringArray(R.array.local_cat_facts)
        val randomIndex = Random.nextInt(factsArray.size)
        return Fact(factsArray[randomIndex])
    }
}