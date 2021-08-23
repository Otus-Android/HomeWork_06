package otus.homework.reactivecats

import io.reactivex.Single
import retrofit2.http.GET

interface CatsService {

    @GET("dogs?number=1")
    fun getCatFact(): Single<List<Fact>>
}
