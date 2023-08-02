package otus.homework.reactivecats.domain

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import otus.homework.reactivecats.domain.models.Cat

/**
 * Репозиторий информации о кошке
 */
interface CatsRepository {

    /** Получить переодически обновляемую информацию о кошке [Cat] в виде [Observable] */
    fun getCats(): Observable<Cat>

    /** Получить информацию о кошке [Cat] в виде [Single] */
    fun getCat(): Single<Cat>
}