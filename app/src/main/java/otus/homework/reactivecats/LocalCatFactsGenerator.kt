package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.concurrent.TimeUnit

class LocalCatFactsGenerator(private val context: Context) {

    fun generateCatFact(): Single<Fact> {
        val factText = context.resources.getStringArray(R.array.local_cat_facts).random()
        val fact = Fact(factText)
        return Single.just(fact)
    }


    fun generateCatFactPeriodically(): Flowable<Fact> {
        return Flowable.interval(2000, TimeUnit.MILLISECONDS)
            .map {
                val factText = context.resources.getStringArray(R.array.local_cat_facts).random()
                Fact(factText)
            }
            .distinctUntilChanged { oldFact, newFact ->
                oldFact.text == newFact.text
            }
    }
}
