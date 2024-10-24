package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.concurrent.TimeUnit

class LocalCatFactsGenerator(
    private val context: Context
) {

    private val facts by lazy {
        Flowable.interval(2000L, TimeUnit.MILLISECONDS)
            .onBackpressureDrop()
            .map { randomFact() }
            .distinctUntilChanged()
    }

    fun generateCatFact(): Single<Fact> {
        return generateCatFactPeriodically().first(randomFact())
    }

    fun generateCatFactPeriodically(): Flowable<Fact> = facts

    private fun randomFact(): Fact = Fact(context.resources.getStringArray(R.array.local_cat_facts).random())
}