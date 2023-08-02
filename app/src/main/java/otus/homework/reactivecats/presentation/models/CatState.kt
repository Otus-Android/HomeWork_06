package otus.homework.reactivecats.presentation.models

import otus.homework.reactivecats.domain.models.Cat
import otus.homework.reactivecats.presentation.mvi.MviState

/**
 * Модель состояния с информацией о кошке
 */
sealed class CatState : MviState {

    /** Состояние бездействия */
    object Idle : CatState()

    /**
     * Состояние наличия информации о кошке
     *
     * @property cat информация о кошке
     */
    data class Success(val cat: Cat) : CatState()

    /**
     * Состояние отсутствия данных о кошке
     *
     * @property message описание причины отсутствия
     * @property isShown признак отображения причины отсутствия данных
     */
    data class Error(val message: String, val isShown: Boolean) : CatState()
}