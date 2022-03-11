package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable.interval
import io.reactivex.Observable
import io.reactivex.Observable.interval
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.operators.single.SingleLift
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    val disposable: CompositeDisposable = CompositeDisposable()

    init {
        getFacts()
    }

    fun getFacts() {
        disposable.add(
            Observable.interval(2, TimeUnit.SECONDS)
                .flatMap {
                    catsService.getCatFact()
                        .onErrorResumeNext { localCatFactsGenerator.generateCatFact() }
                        .toObservable()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ fact ->
                    if (fact != null) _catsLiveData.value = Success(fact)
                    else _catsLiveData.value = Error("Unknown error")

                }, { throwable ->
                    when (throwable) {
                        is SocketTimeoutException -> _catsLiveData.value = ServerError
                        else -> _catsLiveData.value = Error("Unknown error")
                    }
                }
                )
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
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