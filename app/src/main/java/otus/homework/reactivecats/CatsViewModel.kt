package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val disposable = CompositeDisposable()

    private val fetcher = catsService.getCatFact()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    private val subscriber = Consumer<Fact> { _catsLiveData.value = Success(it) }

    private val subscriberError = Consumer<Throwable> {
        _catsLiveData.value = Error(
            it.message ?: context.getString(R.string.default_error_text)
        )
    }

    init {
        fetch()
    }

    private fun fetch() {
        disposable.add(
            fetcher.subscribe(subscriber, subscriberError)
        )
    }

    private fun getFacts() {
        disposable.add(
            Observable.interval(2, TimeUnit.SECONDS)
                .flatMap { fetcher }
                .onErrorResumeNext(localCatFactsGenerator.generateCatFact())
                .subscribe(subscriber, subscriberError)
        )
    }

    override fun onCleared() {
        disposable.clear()

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
