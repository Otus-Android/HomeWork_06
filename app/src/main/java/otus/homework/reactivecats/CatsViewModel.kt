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

    private val compositeDisposable = CompositeDisposable()

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
        compositeDisposable.addAll(
            getFacts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { _catsLiveData.value = Success(it) },
                    { _catsLiveData.value = ServerError }
                )
        )
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

    private fun getFacts(): Flowable<Fact> {
        return Flowable.interval(0,2, TimeUnit.SECONDS)
            .flatMap { catsService.getCatFact().toFlowable() }
            .onErrorResumeNext { _ : Throwable->
                localCatFactsGenerator.generateCatFact().toFlowable()
            }
    }
}

@Suppress("UNCHECKED_CAST")
class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
data object ServerError : Result()