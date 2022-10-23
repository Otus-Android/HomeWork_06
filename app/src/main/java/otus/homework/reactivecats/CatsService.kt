package otus.homework.reactivecats

import io.reactivex.Observable
import retrofit2.http.GET

interface CatsService {

    @GET("/fact/")
    fun getCatFact(): Observable<Fact>
}
