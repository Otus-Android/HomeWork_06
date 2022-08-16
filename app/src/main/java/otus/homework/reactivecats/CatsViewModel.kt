package otus.homework.reactivecats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
        getFacts()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    private fun fetchSingleServerSideFact() {
        val disposable = catsService.getCatFact()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ fact ->
                _catsLiveData.value = Success(fact)
            }, { throwable ->
                _catsLiveData.value = Error(throwable.message)
            })
        compositeDisposable.add(disposable)
    }

    private fun getFacts() {
        val disposable = Flowable.interval(2000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .flatMapSingle {
                catsService.getCatFact()
                    .onErrorResumeNext { localCatFactsGenerator.generateCatFact() }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ fact ->
                _catsLiveData.value = Success(fact)
            }, { throwable ->
                _catsLiveData.value = Error(throwable.message)
            })
        compositeDisposable.add(disposable)
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
data class Error(val message: String?) : Result()
object ServerError : Result()