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
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val compositeDisposable = CompositeDisposable()

    init {
        compositeDisposable.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .map<Result>{ fact -> Success(fact) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { _catsLiveData.value = it },
                    {
                        val result = when (it) {
                            is HttpException -> {
                                Error(it.message())
                            }
                            is IOException -> {
                                ServerError
                            }
                            else -> {
                                Error(it.message ?: context.getString(R.string.default_error_text))
                            }
                        }
                        _catsLiveData.value = result
                    }
                )
        )
        getFacts()
    }

    fun getFacts() {
        compositeDisposable.add(
            Observable.interval(PERIOD_MS, TimeUnit.MILLISECONDS)
                .flatMap {
                    catsService.getCatFact()
                        .onErrorResumeNext(
                            localCatFactsGenerator.generateCatFact()
                        )
                        .toObservable()
                }
                .map<Result> { fact -> Success(fact) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _catsLiveData.value = it
                    },
                    {
                        _catsLiveData.value = Error(it.message.toString())
                    }
                )
        )
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    companion object {
        const val PERIOD_MS = 2000L
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
data object ServerError : Result()