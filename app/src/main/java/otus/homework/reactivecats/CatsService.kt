package otus.homework.reactivecats

import io.reactivex.Single
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET

interface CatsService {

    @GET("random?animal_type=cat")
    fun getCatFact(): Single<Fact>
}