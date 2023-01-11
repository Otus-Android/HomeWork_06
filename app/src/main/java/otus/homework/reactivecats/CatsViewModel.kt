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
    private val localCatFactsGenerator: LocalCatFactsGenerator
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    init {
        getFacts()
    }

    private fun getFacts() {
        compositeDisposable.add(Flowable.interval(0L, 2L, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .flatMapSingle {
                catsService.getCatFact()
                    .onErrorResumeNext { localCatFactsGenerator.generateCatFact() }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                _catsLiveData.value = Loading(true)
            }
            .doAfterTerminate {
                _catsLiveData.value = Loading(false)
            }
            .subscribe({
                _catsLiveData.value = Success(it)
            }, {
                _catsLiveData.value = it.message?.let { message -> ResultError(message) }
            })
            )
    }

    override fun onCleared() {
        super.onCleared()
        if (compositeDisposable.isDisposed.not()) {
            compositeDisposable.dispose()
        }
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class ResultError(val message: String) : Result()
data class Loading(val showProgressBar: Boolean) : Result()
object ServerError : Result()
