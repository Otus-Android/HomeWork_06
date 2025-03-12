package otus.homework.reactivecats.domain

import io.reactivex.Single
import otus.homework.reactivecats.data.Fact

interface CatsRepository {
    fun getCatFact(): Single<Fact>
}