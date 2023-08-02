package otus.homework.reactivecats.data.storage

import io.reactivex.rxjava3.core.Single
import otus.homework.reactivecats.data.models.Fact

/**
 * Локальное хранилище информации о коте
 */
interface CatsStorage {

    /** Получить [Single] со случайым фактом о котом [Fact] */
    fun getRandomCatFact(): Single<Fact>
}