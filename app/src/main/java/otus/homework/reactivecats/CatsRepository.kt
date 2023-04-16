package otus.homework.reactivecats

import io.reactivex.Flowable

interface CatsRepository {

    fun getFact(): Flowable<Fact>

    fun getFactFromNetwork(): Flowable<Fact>
}
