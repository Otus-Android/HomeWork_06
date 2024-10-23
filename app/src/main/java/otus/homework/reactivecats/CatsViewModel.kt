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
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
        getFacts()
    }

    private fun getLocalFact() {
        compositeDisposable.add(
            localCatFactsGenerator.generateCatFact()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { response -> _catsLiveData.value = Success(response) },
                    { _catsLiveData.value = Error(it.localizedMessage.orEmpty()) }
                )
        )
    }

    private fun getLocalFactPeriod() {
        compositeDisposable.add(
            localCatFactsGenerator.generateCatFactPeriodically()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { response -> _catsLiveData.value = Success(response) },
                    { _catsLiveData.value = Error(it.localizedMessage.orEmpty()) }
                )
        )
    }

    private fun getFacts() {
        compositeDisposable.add(
            Observable.interval(2, TimeUnit.SECONDS, Schedulers.io())
                .flatMapSingle { catsService.getCatFact().map { it } }
                .doOnError {
                    getLocalFact()
                }
                .retry()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { _catsLiveData.value = Success(it) },
                    { _catsLiveData.value = Error(it.localizedMessage.orEmpty()) }
                )


        )
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()