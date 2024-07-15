package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private var compositeDisposable: CompositeDisposable? = null

    init {
        compositeDisposable?.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    _catsLiveData.value = Success(result)
                }, { error ->
                    _catsLiveData.value = Error(
                        error.message ?: context.getString(
                            R.string.default_error_text
                        )
                    )
                })
        )
    }

    fun getFacts() {
        val disposable = Observable.interval(2, TimeUnit.SECONDS, Schedulers.io())
            .flatMap { catsService.getCatFact().toObservable() }
            .onErrorResumeNext(ObservableSource {
                localCatFactsGenerator.generateCatFact()
            })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                _catsLiveData.value = Success(result)
            }, { error ->
                _catsLiveData.value = Error(
                    error.message ?: context.getString(
                        R.string.default_error_text
                    )
                )
            })
        compositeDisposable?.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable?.dispose()
        compositeDisposable = null
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