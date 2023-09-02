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
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val disposables = CompositeDisposable()
    private var disposable: Disposable? = null

    private fun handleSuccess(fact: Fact) {
        _catsLiveData.value = Success(fact)
        disposables.clear()
    }

    private fun handleError(t: Throwable) {
        _catsLiveData.value = Error(t.message.toString())
        disposables.clear()
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
        disposable?.dispose()
    }

    init {
        disposable = (
            Observable.interval(0, 2, TimeUnit.SECONDS)
                .subscribe { getFacts() }
        )
    }

    fun getFacts() {
        disposables.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturnItem(Fact(""))
                .flatMap {
                    if (it.text == "") {
                        localCatFactsGenerator.generateCatFact()
                    }
                    else {
                        Single.fromCallable{it}
                    }
                }
                .subscribe(
                    ::handleSuccess,
                    ::handleError
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
        if (modelClass.isAssignableFrom(CatsViewModel::class.java)) {
            CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
        } else {
            throw IllegalArgumentException("Expected CatsViewModel, but ${modelClass::class.java.name}")
        }
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()
