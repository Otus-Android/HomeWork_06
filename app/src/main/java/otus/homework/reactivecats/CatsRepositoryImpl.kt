package otus.homework.reactivecats

import io.reactivex.Flowable
import java.util.concurrent.TimeUnit

class CatsRepositoryImpl(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator
) : CatsRepository {

    private companion object {
        const val SLEEP_MS = 2000L
    }

override fun getFactFromNetwork(): Flowable<Fact> {
    return Flowable
        .interval(SLEEP_MS, TimeUnit.MILLISECONDS)
        .map { catsService.getCatFact() }
        .distinctUntilChanged()
}

    override fun getFact(): Flowable<Fact> {
        return getFactFromNetwork().onErrorResumeNext(localCatFactsGenerator.generateCatFactPeriodically())
    }
}
