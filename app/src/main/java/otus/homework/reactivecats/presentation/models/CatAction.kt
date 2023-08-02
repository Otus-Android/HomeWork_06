package otus.homework.reactivecats.presentation.models

import otus.homework.reactivecats.presentation.mvi.MviAction

/**
 * Модель внешнего намерения на изменение состояния информации о кошке
 */
sealed class CatAction : MviAction {

    /** Намерение начать процесс переодического получения информации о кошке */
    object StartProcess : CatAction()

    /** Намерение отметить отображение ошибки */
    object MarkErrorShown : CatAction()
}