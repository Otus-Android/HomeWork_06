package otus.homework.reactivecats

import io.reactivex.Flowable
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET
import java.util.*

interface CatsService {

    @GET("random?animal_type=cat")
    fun getCatFact(): Flowable<Fact>
}