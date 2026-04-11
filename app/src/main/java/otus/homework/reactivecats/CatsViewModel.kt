package otus.homework.reactivecats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable


class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val disposable = CompositeDisposable()

    init {
        getFacts()
    }

    private fun getFacts() {
        disposable.add(
            localCatFactsGenerator.generateCatFactPeriodically()
                .flatMap(::handleFactRequest)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::onFactSuccess, ::onFactError)
        )
    }

    private fun handleFactRequest(localFact: Fact): Flowable<Fact> {
        return catsService.getCatFact()
            .onErrorResumeNext { Single.just(localFact) }
            .toFlowable()
    }

    private fun onFactSuccess(fact: Fact) {
        _catsLiveData.value = Success(fact)
    }

    private fun onFactError(throwable: Throwable) {
        _catsLiveData.value = Error(throwable.message!!)
    }


    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()