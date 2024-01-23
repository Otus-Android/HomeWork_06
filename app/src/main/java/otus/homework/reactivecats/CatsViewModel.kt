package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catFactsSubject =
        BehaviorSubject.createDefault<CatFactUiState>(CatFactUiState.Loading)
    val catFactsObservable: Observable<CatFactUiState> = _catFactsSubject

    private val viewModelDisposables = CompositeDisposable()

    init {
        getFacts()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { fact ->
                    _catFactsSubject.onNext(CatFactUiState.Success(fact))
                },
                { error ->
                    _catFactsSubject.onNext(
                        CatFactUiState.Error(
                            error.message or context.getString(R.string.default_error_text)
                        )
                    )
                }
            )
            .addTo(viewModelDisposables)
    }

    private fun getFacts(): Observable<Fact> {
        return Observable.interval(0, 2000, TimeUnit.MILLISECONDS)
            .concatMapSingle {
                catsService
                    .getCatFact()
                    .onErrorResumeNext(
                        localCatFactsGenerator.generateCatFact()
                    )
            }
            .subscribeOn(Schedulers.io())
    }

    override fun onCleared() {
        viewModelDisposables.clear()
        super.onCleared()
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed interface CatFactUiState {
    data object Loading : CatFactUiState
    data class Success(val fact: Fact) : CatFactUiState
    data class Error(val message: String) : CatFactUiState
}