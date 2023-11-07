package otus.homework.reactivecats

import retrofit2.Call
import retrofit2.http.GET

interface CatsService {

    @GET("fact")
    fun getCatFact(): Call<Fact>
}