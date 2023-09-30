package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel(), LifecycleObserver {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val disposable = CompositeDisposable()

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onLifeCycleStart() {
        getFacts()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onLifeCycleStop() {
        disposable.dispose()
    }

    private fun getFacts() = Observable.interval(0, 2000L, TimeUnit.MILLISECONDS)
        .flatMap {
            catsService.getCatFact()
                .onErrorResumeNext { localCatFactsGenerator.generateCatFact() }
                .toObservable()
        }
        .subscribeOn(Schedulers.io()).distinctUntilChanged()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            { fact -> _catsLiveData.value = Success(fact) },
            { exception ->
                _catsLiveData.value = Error(
                    exception.message ?: context.getString(R.string.default_error_text)
                )
            })

    override fun onCleared() {
        super.onCleared()
        if (!disposable.isDisposed) {
            disposable.dispose()
        }
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