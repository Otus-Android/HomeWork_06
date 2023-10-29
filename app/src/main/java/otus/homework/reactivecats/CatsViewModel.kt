package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    val catsService: CatsService,
    val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val compositeDisposable = CompositeDisposable()

    init {
        val disposable = catsService.getCatFact()
            .toObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { fact -> onResponse(fact) },
                { throwable -> onError(throwable.message) }
            )
        compositeDisposable.add(disposable)
        getFacts()
    }

    fun getFacts() {
        val disposableFlowable = Observable
            .interval(2000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap {
                catsService.getCatFact()
                    .onErrorResumeNext(localCatFactsGenerator.generateCatFact()).toObservable()
            }
            .subscribe(
                { fact -> onResponse(fact) },
                { throwable -> onError(throwable.message) }
            )
        compositeDisposable.add(disposableFlowable)
    }

    private fun onResponse(response: Fact) {
        _catsLiveData.value = Success(response)
    }

    private fun onError(errorMessage: String?) {
        _catsLiveData.value = Error(errorMessage)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String?) : Result()
object ServerError : Result()
