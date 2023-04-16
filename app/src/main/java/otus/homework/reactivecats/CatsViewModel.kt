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
import otus.homework.reactivecats.LocalCatFactsGenerator.Companion.CAT_FACT_INTERVAL
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class CatsViewModel(
    context: Context,
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val disposables = CompositeDisposable()

    init {
        val disposable = catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                /* onSuccess = */ { fact: Fact ->
                    _catsLiveData.value = Success(fact)
                },
                /* onError = */ { e: Throwable ->
                    if (e is HttpException) {
                        val error = e.response()?.errorBody()?.string()
                            ?: context.getString(R.string.default_error_text)
                        _catsLiveData.value = Error(error)
                    } else {
                        _catsLiveData.value = ServerError
                    }
                })
        disposables.add(disposable)
    }

    fun getFacts() {
        val disposable = Observable
            .interval(CAT_FACT_INTERVAL, TimeUnit.MILLISECONDS)
            .flatMap {
                catsService.getCatFact()
                    .onErrorResumeNext(localCatFactsGenerator.generateCatFact())
                    .toObservable()
            }
            .distinctUntilChanged()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                _catsLiveData.value = Success(it)
            }
        disposables.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
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
        CatsViewModel(context, catsRepository, localCatFactsGenerator) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()