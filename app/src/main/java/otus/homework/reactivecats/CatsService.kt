package otus.homework.reactivecats

import retrofit2.Call
import retrofit2.http.GET

interface CatsService {

    @GET("dogs?number=1")
    fun getCatFact(): Call<List<Fact>>
}