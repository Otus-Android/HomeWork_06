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
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val compositeDisposable = CompositeDisposable()

    init {
        compositeDisposable.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { fact -> setCatsInfo(fact) },
                    { _catsLiveData.value = ServerError }
                ))
    }

    fun getFacts() {
        compositeDisposable.add(
            Observable.interval(0, 2, TimeUnit.SECONDS)
                .flatMap {
                    catsService.getCatFact()
                        .onErrorResumeNext(
                            localCatFactsGenerator.generateCatFact()
                        )
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { fact -> setCatsInfo(fact) },
                    {
                        _catsLiveData.value = ServerError
                    }
                )
        )
    }

    private fun setCatsInfo(fact: Fact?) {
        if (fact != null) {
            _catsLiveData.value = Success(fact)
        } else {
            _catsLiveData.value =
                Error(
                    context.getString(
                        R.string.default_error_text
                    )
                )
        }
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
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