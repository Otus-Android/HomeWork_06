package otus.homework.reactivecats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit


class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val subscriptions: CompositeDisposable
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
        getFacts()
    }

    private fun getFacts() {
        val initialDelay = 0L
        val period = 2L
        val timeUnit = TimeUnit.SECONDS

        val disposable = Observable.interval(initialDelay, period, timeUnit)
            .flatMap {
                catsService.getCatFact().toObservable()
            }
            .onErrorResumeNext(
                localCatFactsGenerator.generateCatFactPeriodically().toObservable()
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::doOnSuccessReceiveFact, ::doOnError)

        subscriptions.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()

        subscriptions.clear()
    }

    private fun doOnSuccessReceiveFact(fact: Fact) {
        _catsLiveData.value = Success(fact)
    }

    private fun doOnError(throwable: Throwable) {
        throwable.printStackTrace()
        _catsLiveData.value = Error(message = throwable.localizedMessage)
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val subscriptions: CompositeDisposable
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, subscriptions) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String?) : Result()