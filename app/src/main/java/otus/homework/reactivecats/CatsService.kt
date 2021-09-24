package otus.homework.reactivecats

import io.reactivex.Single
import retrofit2.http.GET

interface CatsService {

    @GET("facts")
    fun getCatFact(): Single<List<Fact>>
}