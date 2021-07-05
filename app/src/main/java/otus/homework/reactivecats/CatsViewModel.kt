package otus.homework.reactivecats

import android.util.Log
import androidx.lifecycle.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator
) : ViewModel(), LifecycleObserver {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private var getFactDisposable: Disposable? = null

    init {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onLifeCycleStart() {
        getFacts()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onLifeCycleStop() {
        getFactDisposable?.dispose()
    }

    private fun getFacts() {
        getFactDisposable = Observable
            .interval(2000, TimeUnit.MILLISECONDS)
            .doOnNext { Log.d(this::class.simpleName, "getFacts() onNext $it") }
            .doOnError { Log.d(this::class.simpleName, "getFacts() onError ${it.localizedMessage ?: "Unknown error"}") }
            .flatMap { catsService.getCatFact().onErrorResumeNext { localCatFactsGenerator.generateCatFact() }.toObservable() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ fact ->
                _catsLiveData.value = Success(fact)
            }, { ex ->
                _catsLiveData.value = Error(ex.localizedMessage ?: "Unknown error")
            })
    }

    private fun getFact() {

        getFactDisposable = catsService.getCatFact()
            .repeatWhen { completed ->
                completed.delay(5, TimeUnit.SECONDS)
            }
            .doOnError { ex -> _catsLiveData.value = Error(ex.localizedMessage ?: "Unknown error") }
            .retryWhen { errorObservable ->
                errorObservable.delay(5, TimeUnit.SECONDS)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { _catsLiveData.value = Success(it) }
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
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