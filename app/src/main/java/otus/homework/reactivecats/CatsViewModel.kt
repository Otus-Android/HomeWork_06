package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private var disposable: Disposable

    init {
        disposable = catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { fact -> _catsLiveData.value = Success(fact) },
                {
                    _catsLiveData.value = ServerError
                }
            )
    }

    fun getFacts() {
        disposable = Observable.interval(2, TimeUnit.SECONDS)
            .flatMap {
                catsService.getCatFact().onErrorResumeNext(localCatFactsGenerator.generateCatFact().toObservable())
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {fact ->
                _catsLiveData.value = Success(fact)
            }
    }

    fun getRandom() {
        disposable = localCatFactsGenerator.generateCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { value ->
                _catsLiveData.value = Success(value)
            }
    }

    fun getRandomPeriodically() {
        disposable = localCatFactsGenerator.generateCatFactPeriodically()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .onBackpressureLatest()
            .subscribe { value ->
                _catsLiveData.value = Success(value)
            }
    }


    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
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