package otus.homework.reactivecats.data

import io.reactivex.Single
import otus.homework.reactivecats.domain.CatsRepository

class CatsRepositoryImpl : BaseRepository(), CatsRepository {

    override fun getCatFact(): Single<Fact> = netService.getCatFact()
}