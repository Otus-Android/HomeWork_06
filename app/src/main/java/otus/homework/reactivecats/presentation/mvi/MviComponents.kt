package otus.homework.reactivecats.presentation.mvi

/** Маркерный интерфейс внешних намерений на изменения */
interface MviAction

/** Маркерный интерфейс внутренних изменений состояний */
interface MviChange

/** Маркерный интерфейс ui состояний */
interface MviState

/** Производитель новых состояний [MviState] на основе текущего значения и внутреннего изменения [MviChange] */
typealias MviReducer<MviState, MviChange> = (MviState, MviChange) -> MviState