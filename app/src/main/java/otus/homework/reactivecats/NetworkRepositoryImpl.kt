package otus.homework.reactivecats

class NetworkRepositoryImpl(
    private val catsService: CatsService
): NetworkRepository {
    override fun getCatFacts() = catsService.getCatFact()
}