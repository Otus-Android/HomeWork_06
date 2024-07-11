package otus.homework.reactivecats

import retrofit2.http.GET
import io.reactivex.Single

interface CatsService {

    @GET("random?animal_type=cat")
    fun getCatFact(): Single<Fact>
}