package otus.homework.reactivecats.domain

import io.reactivex.Single

interface CatsRepository {
    fun getCatFact(): Single<String>
}