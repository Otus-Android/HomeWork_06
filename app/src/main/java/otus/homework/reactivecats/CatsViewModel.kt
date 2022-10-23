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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val compositeDisposable = CompositeDisposable()

    init {
        getFacts()
    }

    private fun getFact() {
        compositeDisposable.add(
            catsService.getCatFact()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { response ->
                        _catsLiveData.value = Success(response)
                    },
                    { error ->
                        when (error) {
                            is SocketTimeoutException -> _catsLiveData.value = ServerError
                            else -> _catsLiveData.value = Error(error.stackTraceToString())
                        }
                    }
                )
        )
    }

    private fun getFacts() {
        compositeDisposable.add(
            Observable
                .interval(2000, TimeUnit.MILLISECONDS)
                .flatMap {
                    catsService.getCatFact().onErrorResumeNext {
                        localCatFactsGenerator.generateCatFact()
                    }.toObservable()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { response ->
                        _catsLiveData.value = Success(response)
                    },
                    { error ->
                        when (error) {
                            is SocketTimeoutException -> _catsLiveData.value = ServerError
                            else -> _catsLiveData.value = Error(error.stackTraceToString())
                        }
                    }
                )
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()
