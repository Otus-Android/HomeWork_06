package otus.homework.reactivecats

import io.reactivex.Single

interface NetworkRepository {
    fun getCatFacts(): Single<Fact>
}