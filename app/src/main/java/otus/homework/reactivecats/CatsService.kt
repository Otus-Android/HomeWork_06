package otus.homework.reactivecats

import io.reactivex.Single
import retrofit2.http.GET

interface CatsService {

    @GET("/fact?max_length=140")
    fun getCatFact(): Single<Fact>
}