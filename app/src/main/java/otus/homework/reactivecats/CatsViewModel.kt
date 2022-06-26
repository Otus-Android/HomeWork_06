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
import java.util.*
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val disposables: CompositeDisposable,
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
//        val disposable = catsService.getCatFact()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({
//                _catsLiveData.value = Success(it)
//            }, {
//                _catsLiveData.value = Error(it.message ?: context.getString(R.string.default_error_text))
//            })
//        disposables.add(disposable)
        getFacts()
    }

    fun getFacts() {
        val disposable = Observable.interval(2000, TimeUnit.MILLISECONDS)
            .flatMap {
                catsService.getCatFact()
                    .onErrorResumeNext { localCatFactsGenerator.generateCatFact() }.toObservable()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::getFactSuccess, ::getFactError)

        disposables.add(disposable)
    }

    private fun getFactSuccess(fact: Fact) {
        _catsLiveData.value = Success(fact)
    }

    private fun getFactError(error: Throwable) {
        _catsLiveData.value = Error(error.message ?: context.getString(R.string.default_error_text))
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val compositeDisposable: CompositeDisposable,
    private val context: Context
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, compositeDisposable, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()