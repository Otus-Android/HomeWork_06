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

    private val disposables = CompositeDisposable()
    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
        disposables.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { facts ->
                        if (facts.isNotEmpty() && facts[0].text != "null") {
                            _catsLiveData.value = Success(facts[0])
                        } else {
                            _catsLiveData.value = Error(context.getString(R.string.default_error_text))
                        }
                    },
                    { _catsLiveData.value = ServerError }
                )
        )
    }

    private fun getFacts() {
        val disposable =
            Observable.interval(0, 2, TimeUnit.SECONDS)
                .flatMap {
                    catsService.getCatFact()
                        .onErrorResumeNext { localCatFactsGenerator.generateCatFact() }
                        .toObservable()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { facts ->
                    if (facts.isNotEmpty() && facts[0].text != "null") {
                        _catsLiveData.value = Success(facts[0])
                    } else {
                        _catsLiveData.value = Error(context.getString(R.string.default_error_text))
                    }
                }
        disposables.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
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
