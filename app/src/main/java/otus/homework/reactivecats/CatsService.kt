package otus.homework.reactivecats

import retrofit2.Call
import io.reactivex.Single
import retrofit2.http.GET

interface CatsService {

    //@GET("random?animal_type=cat")
    @GET("fact")
    fun getCatFact(): Single<Fact>
}