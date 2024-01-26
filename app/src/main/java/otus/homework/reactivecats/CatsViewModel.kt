package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val compositeDisposable = CompositeDisposable()
    private val defaultErrorText = context.getString(R.string.default_error_text)

    init {
        catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                if (response.isSuccessful && response.body() != null) {
                    Success(response.body()!!)
                } else {
                    Error(response.errorBody()?.string() ?: defaultErrorText)
                }
            }
            .addSubscribe(
                { _catsLiveData.value = it },
                { _catsLiveData.value = ServerError }
            )
    }

    fun getFacts() {
        Observable.interval(0, 2, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.computation())
            .flatMapSingle { catsService.getCatFact() }
            .map { response ->
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!
                } else {
                    throw RuntimeException("Wrong response: ${response.errorBody()?.string()}")
                }
            }
            .onErrorResumeNext(
                localCatFactsGenerator.generateCatFactPeriodically().toObservable()
            )
            .observeOn(AndroidSchedulers.mainThread())
            .addSubscribe(
                { _catsLiveData.value = Success(it) },
                { _catsLiveData.value = ServerError }
            )
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    private fun <T> Observable<T>.addSubscribe(
        onSuccess: (T) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        compositeDisposable.add(
            this.subscribe(
                { onSuccess.invoke(it) },
                { onError.invoke(it) }
            )
        )
    }

    private fun <T> Single<T>.addSubscribe(
        onSuccess: (T) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        compositeDisposable.add(
            this.subscribe(
                { onSuccess.invoke(it) },
                { onError.invoke(it) }
            )
        )
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
