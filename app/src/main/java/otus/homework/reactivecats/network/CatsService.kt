package otus.homework.reactivecats.network

import io.reactivex.Single
import otus.homework.reactivecats.Fact
import retrofit2.Call
import retrofit2.http.GET

interface CatsService {

    @GET("random?animal_type=cat")
    fun getCatFact(): Single<Fact>
}