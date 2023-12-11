package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.WeakReference
import java.util.concurrent.Flow
import java.util.concurrent.Flow.Subscriber
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val disposables = mutableListOf<Disposable>()
    private val defaultErrorMessage = context.getString(R.string.default_error_text)


    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData


    init {
        disposables.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { _catsLiveData.value = Success(it) },
                    { _catsLiveData.value = Error(it.message ?: defaultErrorMessage) }
                )
        )
    }

    fun getFacts() {
        disposables.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .onErrorResumeNext(
                    localCatFactsGenerator
                        .generateCatFact()
                        .toObservable()
                )
                .observeOn(AndroidSchedulers.mainThread())
                .delay(2L, TimeUnit.SECONDS)
                .repeat()
                .subscribe(
                    { fact -> _catsLiveData.postValue(Success(fact)) },
                    { throwable ->
                        _catsLiveData.postValue(
                            Error(throwable.message ?: defaultErrorMessage)
                        )
                    }
                )
        )
    }

    override fun onCleared() {
        disposables.onEach { it.dispose() }
    }
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
data object ServerError : Result()