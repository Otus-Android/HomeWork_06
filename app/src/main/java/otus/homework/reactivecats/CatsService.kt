package otus.homework.reactivecats

import retrofit2.Call
import retrofit2.http.GET

interface CatsService {

    @GET("random?animal_type=cat")
    fun getCatFact(): Call<Fact>
}
