package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
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
       getFacts(context)
    }

    fun getFacts(context: Context) {

        val observable = Observable.interval(2, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .flatMap { catsService.getCatFact() }
            .doOnError {
                generateCatFactWhileError()
//                _catsLiveData.postValue(ServerError)
            }
            .retry()
            .subscribe(
                { fact ->
                    _catsLiveData.postValue(Success(fact))
                }, {
                    generateCatFactWhileError()
                })


        compositeDisposable.add(observable)
    }

    fun generateCatFactWhileError() {
        localCatFactsGenerator.generateCatFact()
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe(object: SingleObserver<Fact> {

                override fun onSubscribe(d: Disposable) {
                }

                override fun onSuccess(fact: Fact) {
                    _catsLiveData.postValue(Success(fact))
                }
                override fun onError(e: Throwable) {
                }
            })
    }

    override fun onCleared() {
        compositeDisposable.dispose()
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
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()