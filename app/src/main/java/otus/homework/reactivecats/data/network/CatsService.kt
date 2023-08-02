package otus.homework.reactivecats.data.network

import io.reactivex.rxjava3.core.Single
import otus.homework.reactivecats.data.models.Fact
import retrofit2.http.GET

/**
 * Сервис получения информации о кошках
 */
interface CatsService {

    /** Получить случайных факт о кошке [Fact] */
    @GET("random?animal_type=cat")
    fun getCatFact(): Single<Fact>
}