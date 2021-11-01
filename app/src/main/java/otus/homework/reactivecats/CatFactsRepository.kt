package otus.homework.reactivecats

import io.reactivex.Flowable
import java.util.concurrent.TimeUnit

class CatFactsRepository(
    private val service: CatsService,
    private val localGenerator: LocalCatFactsGenerator
) {
    fun getCatFact(): Flowable<Fact> {
        return Flowable.interval(0, 2, TimeUnit.SECONDS)
            .flatMapSingle {
                service.getCatFact()
                    .timeout(TIMEOUT, TimeUnit.SECONDS)
                    .onErrorResumeNext(localGenerator.generateCatFact())
            }
    }

    private companion object {
        const val TIMEOUT = 5L
    }
}