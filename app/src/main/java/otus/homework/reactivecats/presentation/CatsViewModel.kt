package otus.homework.reactivecats.presentation

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import otus.homework.reactivecats.data.Fact
import otus.homework.reactivecats.domain.CatsInteractor

class CatsViewModel(
    private val appContext: Context,
    private val catsInteractor: CatsInteractor,
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val compositeDisposable = CompositeDisposable()

    init {
        compositeDisposable.add(
            Single.defer { catsInteractor.getCatFact() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(catFactObserver)
        )
    }

    private val catFactObserver: DisposableSingleObserver<Fact>
        get() = object : DisposableSingleObserver<Fact>() {
            override fun onSuccess(fact: Fact) {
                Log.e(null, "[catFactObserver]: remote fact obtained")
                _catsLiveData.value = Result.Success(fact)
            }

            override fun onError(e: Throwable) {
                when (e as Exception) {

                }
                Log.e(null, "[catFactObserver]: ${e.message}")
                val fact = catsInteractor.getLocalCatFact(appContext).blockingGet()
//                val fact = catsInteractor.getLocalCatFactPeriodically(appContext).toObservable().blockingFirst()
                _catsLiveData.value = Result.Error(fact, "Remote fact unavailable, local generated")
            }
        }

    override fun onCleared() {
        super.onCleared()
        if (compositeDisposable.isDisposed.not()) {
            compositeDisposable.dispose()
        }
    }
}

sealed class Result {
    data class Success(val fact: Fact) : Result()
    data class Error(val fact: Fact, val message: String) : Result()
    data object ServerError : Result()
}