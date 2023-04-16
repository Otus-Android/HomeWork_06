package otus.homework.reactivecats

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.lang.Thread.sleep

class CatsRepositoryImpl(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator
) : CatsRepository {

    private companion object {
        const val SLEEP_MS = 2000L
    }

    override fun getFactFromNetwork(): Flowable<Fact> {
        return Flowable
            .create<Fact>(
                {
                    while (true) {
                        val success = catsService.getCatFact()
                        it.onNext(success)
                        sleep(SLEEP_MS)
                    }
                },
                BackpressureStrategy.BUFFER
            )
            .distinctUntilChanged()
    }

    override fun getFact(): Flowable<Fact> {
        return getFactFromNetwork().onErrorResumeNext(localCatFactsGenerator.generateCatFactPeriodically())
    }
}
