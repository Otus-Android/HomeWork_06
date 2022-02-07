package otus.homework.reactivecats

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit.MILLISECONDS

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    var disposable: Disposable? = null

    init {
        disposable = getFacts()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error -> _catsLiveData.value = Error(error.localizedMessage ?: "Some error") }
            .subscribe { result ->
                Log.d("Reactive Cats", "New fact: ${result.text}")
                _catsLiveData.value = Success(result)
            }
    }

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }

    fun getFacts(): Flowable<Fact> =
        Flowable.interval(2000, MILLISECONDS).flatMap { catsService.getCatFact().toFlowable() }
            .onErrorResumeNext(localCatFactsGenerator.generateCatFactPeriodically())
            .doOnError { Log.e("Reactive Cats", "${it.message}") }
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
data class Error(val message: String) : Result()
object ServerError : Result()