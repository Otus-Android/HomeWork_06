package otus.homework.reactivecats.presentation.mvi

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import otus.homework.reactivecats.utils.rxjava.RxSchedulers

/**
 * [ViewModel] для реализации `MVI`, являющаяся модифицированный аналогом библиотеки `Roxie`.
 *
 * `Roxie` использует реализацию на основе `RxJava2`.
 * В данной модификации используется `RxJava3` с дополнительными изменениями.
 *
 * @param schedulers обертка получения `Scheduler`-ов
 */
abstract class MviViewModel<A : MviAction, S : MviState, C : MviChange>(
    private val schedulers: RxSchedulers
) : ViewModel() {

    /** UI состояние [S] в виде [LiveData] */
    val uiState: LiveData<S> get() = _uiState
    private val _uiState = MutableLiveData<S>()

    /** Начальное UI состояние [S] */
    protected abstract val initialState: S

    /** Производитель новых внешних намерений на изменения [A] */
    protected val actions: Observable<A> get() = _actions
    private val _actions: PublishSubject<A> = PublishSubject.create()

    /** Производитель внутренних изменений [C]*/
    protected abstract val binder: Observable<C>

    /** Производитель новых состояний [S] на основе текущего значения и внутреннего изменения [C] */
    protected abstract val reducer: MviReducer<S, C>

    /** Хранилище набора `Disposable` */
    protected val disposables: CompositeDisposable = CompositeDisposable()

    /** Запустить обработку компонентов `MVI` */
    protected fun startMvi() {
        binder.scan(initialState, reducer).distinctUntilChanged()
            .observeOn(schedulers.main)
            .subscribe(_uiState::setValue) { e -> Log.e(TAG, "Error: $e") }
            .also { disposables.add(it) }
    }

    /** Отправить намерение [A] (единственный способ внести изменения состояния) */
    fun dispatch(action: A) = _actions.onNext(action)

    override fun onCleared() = disposables.clear()

    protected companion object {
        private const val TAG = "MviViewModel"
    }
}