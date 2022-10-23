package otus.homework.reactivecats

import io.reactivex.Single
import otus.homework.reactivecats.Constants.USE_RESERVE_SERVER
import retrofit2.http.GET

interface CatsService {

    @GET("random?animal_type=cat")
    fun getCatFact(): Single<Fact>

    @GET("https://catfact.ninja/fact")
    fun getCatFactReserve(): Single<FactReserveDto>

    @JvmDefault
    fun getCatFactUniversal(): Single<Fact> {
        return if (USE_RESERVE_SERVER) getCatFactReserve().map { it.toFact() } else getCatFact()
    }
}