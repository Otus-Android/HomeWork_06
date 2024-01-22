package otus.homework.reactivecats

import io.reactivex.Flowable
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.GET

interface CatsService {

    @GET("random?animal_type=cat")
    fun getCatFact(): Flowable<Fact>
}