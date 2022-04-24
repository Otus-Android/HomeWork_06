package otus.homework.reactivecats

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
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator
) : ViewModel() {

    private val mCompositeDisposable = CompositeDisposable()

    private val mCatsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = mCatsLiveData

    init {
        mCompositeDisposable += catsService.getCatFact()
            .subscribeToCatsObservable()
    }

    override fun onCleared() {
        super.onCleared()
        mCompositeDisposable.dispose()
    }

    fun getFacts() {
        mCompositeDisposable += Observable.interval(2000, TimeUnit.MILLISECONDS)
            .flatMap { catsService.getCatFact() }
            .onErrorResumeNext(localCatFactsGenerator.generateCatFact().toObservable())
            .subscribeToCatsObservable()
    }

    private fun Observable<Fact>.subscribeToCatsObservable() =
        subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError(::handleOnError)
            .doOnNext(::handleOnNext)
            .subscribe()

    private fun handleOnNext(fact: Fact) {
        mCatsLiveData.value = Success(fact)
    }

    private fun handleOnError(throwable: Throwable) {
        mCatsLiveData.value = Error(throwable.message)
    }

    private operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
        add(disposable)
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