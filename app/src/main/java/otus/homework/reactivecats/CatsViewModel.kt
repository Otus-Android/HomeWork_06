package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    catsService: CatsService,
    localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val compositeDisposable = CompositeDisposable()

    init {
        compositeDisposable.add(getFacts(catsService, localCatFactsGenerator, context))
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    private fun getFacts(
        catsService: CatsService,
        localCatFactsGenerator: LocalCatFactsGenerator,
        context: Context
    ): Disposable {
        return Observable.interval(2000, TimeUnit.MILLISECONDS)
            .flatMap {
                catsService.getCatFact()
            }
            .onErrorResumeNext(localCatFactsGenerator.generateCatFact().toObservable())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { fact -> _catsLiveData.value = Success(fact) },
                { throwable: Throwable ->
                    _catsLiveData.value = Error(throwable.message ?: context.getString(R.string.default_error_text))
                }
            )
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