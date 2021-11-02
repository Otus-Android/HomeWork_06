package otus.homework.reactivecats

import io.reactivex.Single
import java.util.concurrent.TimeUnit

class CatFactsRepository(
    private val service: CatsService,
    private val localGenerator: LocalCatFactsGenerator
) {
    fun getCatFact(): Single<Fact> {
        return service.getCatFact()
            .timeout(TIMEOUT, TimeUnit.SECONDS)
            .onErrorResumeNext(localGenerator.generateCatFact())
    }

    private companion object {
        const val TIMEOUT = 5L
    }
}