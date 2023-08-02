package otus.homework.reactivecats.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.core.Observable
import otus.homework.reactivecats.domain.CatsRepository
import otus.homework.reactivecats.presentation.models.CatAction
import otus.homework.reactivecats.presentation.models.CatChange
import otus.homework.reactivecats.presentation.models.CatState
import otus.homework.reactivecats.presentation.mvi.MviReducer
import otus.homework.reactivecats.presentation.mvi.MviViewModel
import otus.homework.reactivecats.utils.StringProvider
import otus.homework.reactivecats.utils.rxjava.RxSchedulers

/**
 * [ViewModel] получения информации о случайном коте
 *
 * @param repository репозиторий информации о кошке
 * @param provider поставщик строковых значений
 * @param schedulers обертка получения `Scheduler`-ов
 */
class CatsViewModel(
    private val repository: CatsRepository,
    private val provider: StringProvider,
    private val schedulers: RxSchedulers
) : MviViewModel<CatAction, CatState, CatChange>(schedulers) {

    override val initialState: CatState = CatState.Idle
    override val binder: Observable<CatChange> =
        Observable.merge(bindStartProcess(), bindMarkErrorShown())
    override val reducer: MviReducer<CatState, CatChange> =
        { state, change -> reduce(state, change) }

    private fun bindStartProcess(): Observable<CatChange> =
        actions.ofType(CatAction.StartProcess::class.java)
            .switchMap { repository.getCats() }
            .map { CatChange.Updated(it) as CatChange }
            .onErrorReturn { e -> CatChange.Error(e) }
            .startWithItem(CatChange.Started)

    private fun bindMarkErrorShown(): Observable<CatChange> =
        actions.ofType(CatAction.MarkErrorShown::class.java)
            .map { CatChange.HideError }

    private fun reduce(state: CatState, change: CatChange) = when (change) {
        CatChange.Started -> CatState.Idle
        is CatChange.Updated -> CatState.Success(change.cat)
        is CatChange.Error -> CatState.Error(message = change.error.toString(), isShown = false)
        CatChange.HideError -> if (state is CatState.Error) state.copy(isShown = true) else state
    }

    init {
        startMvi()
    }

    companion object {

        /**
         * Получить фабрику по созданию [CatsViewModel].
         *
         * @param repository репозиторий информации о кошке
         * @param provider поставщик строковых значений
         * @param schedulers обертка получения `Scheduler`-ов
         */
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            repository: CatsRepository, provider: StringProvider, schedulers: RxSchedulers
        ) = object : ViewModelProvider.NewInstanceFactory() {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CatsViewModel(repository, provider, schedulers) as T
        }
    }
}