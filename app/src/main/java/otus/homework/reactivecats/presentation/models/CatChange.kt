package otus.homework.reactivecats.presentation.models

import otus.homework.reactivecats.domain.models.Cat
import otus.homework.reactivecats.presentation.mvi.MviChange

/**
 * Модель внутреннего изменения состояния информации о кошке
 */
sealed class CatChange : MviChange {

    /** Индикатор запуска процесса переодического получения информации о кошке */
    object Started : CatChange()

    /**
     * Обновленная информация о кошке
     *
     * @property cat информация о кошке
     */
    data class Updated(val cat: Cat) : CatChange()

    /**
     * Ошибка процесса переодического получения информации о кошке
     *
     * @property error ошибка
     */
    data class Error(val error: Throwable) : CatChange()

    /** Индикатор скрытия сообщения об ошибке */
    object HideError : CatChange()
}