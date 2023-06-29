package otus.homework.reactivecats

import io.reactivex.Single

interface CatsRepository {

    fun getCatFact(): Single<Fact>

}