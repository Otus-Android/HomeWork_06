package otus.homework.reactivecats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val networkErrorMessage: String,
    private val defaultErrorMessage: String
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val compositeDisposable = CompositeDisposable()

    init {
        getCatFacts()
    }

    fun getCatFacts() {
        val disposable = Flowable.interval(2, TimeUnit.SECONDS)
            .flatMapSingle {
                catsService.getCatFact()
                    .map<Result> { Success(it) }
                    .onErrorResumeNext(
                        localCatFactsGenerator.generateCatFact().map<Result> {
                            SuccessWithWarning(it, networkErrorMessage)
                        })
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                           _catsLiveData.value = it
                       }, {
                           _catsLiveData.value = Error(it.message ?: defaultErrorMessage)
                       })
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val networkErrorMessage: String,
    private val defaultErrorMessage: String
) :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, networkErrorMessage, defaultErrorMessage) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class SuccessWithWarning(val fact: Fact, val message: String) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()