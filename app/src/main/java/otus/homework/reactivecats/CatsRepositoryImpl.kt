package otus.homework.reactivecats

import io.reactivex.Single

class CatsRepositoryImpl(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
) : CatsRepository {

    override fun getCatFact(): Single<Fact> =
        catsService.getCatFact()
            .onErrorResumeNext(
                localCatFactsGenerator.generateCatFact()
            )

}
