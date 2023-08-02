package otus.homework.reactivecats.data

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import otus.homework.reactivecats.data.converters.CatConverter
import otus.homework.reactivecats.data.models.Fact
import otus.homework.reactivecats.data.network.CatsService
import otus.homework.reactivecats.data.storage.CatsStorage
import otus.homework.reactivecats.domain.CatsRepository
import otus.homework.reactivecats.domain.models.Cat
import java.util.concurrent.TimeUnit

/**
 * Реализация репозитория информации о кошке
 *
 * @param service сервис получения информации о кошках
 * @param storage локальное хранилище информации о коте
 * @param converter конвертер данных из [Fact] в данные с информацией о кошке [Cat]
 */
class CatsRepositoryImpl(
    private val service: CatsService,
    private val storage: CatsStorage,
    private val converter: CatConverter
) : CatsRepository {

    override fun getCats(): Observable<Cat> =
        Observable.interval(INTERVAL, TimeUnit.MILLISECONDS)
            .flatMapSingle { getCat() }

    override fun getCat(): Single<Cat> =
        service.getCatFact()
            .onErrorResumeNext { storage.getRandomCatFact() }
            .map { fact -> converter.convert(fact) }

    private companion object {
        const val INTERVAL = 2000L
    }
}