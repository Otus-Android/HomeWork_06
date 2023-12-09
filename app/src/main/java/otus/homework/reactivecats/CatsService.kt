package otus.homework.reactivecats

import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET

interface CatsService {

    @GET("random?animal_type=cat")
    fun getCatFact(): Call<Fact>

    @GET("random?animal_type=cat")
    fun getCatFactSingle(): Single<Fact>

    @GET("random?animal_type=cat")
    fun getCatFactObservable(): Observable<Fact>
}