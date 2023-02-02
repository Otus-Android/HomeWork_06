package otus.homework.reactivecats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val compositeDisposable = CompositeDisposable()

    init {
        compositeDisposable.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .doOnNext {
                    emitLD(Success(it))
                }
                .doOnError {
                    emitLD(Error(it.message))
                }
                .onExceptionResumeNext {
                    emitLD(ServerError)
                }
                .subscribe()
        )
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

    private fun emitLD(result: Result) {
        _catsLiveData.postValue(result)
    }

    private fun getFacts() {
        compositeDisposable.add(
            Observable.interval(2000, TimeUnit.MILLISECONDS)
                .doOnNext {
                    catsService.getCatFact()
                        .subscribeOn(Schedulers.io())
                        .doOnNext {
                            emitLD(Success(it))
                        }
                        .onExceptionResumeNext {
                            localCatFactsGenerator.generateCatFact()
                                .subscribeOn(Schedulers.io())
                                .doOnSuccess {
                                    emitLD(Success(it))
                                }
                                .subscribe()
                        }
                        .subscribe()
                }
                .subscribe()
        )
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String?) : Result()
object ServerError : Result()