package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class CatsViewModel(
    val catsService: CatsService,
    val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
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
        getFactDisposable = catsService.getCatFact()
            .onErrorResumeNext(localCatFactsGenerator.generateCatFact().toObservable())
            .repeatWhen { completed ->
                completed.delay(2000, TimeUnit.MILLISECONDS)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { _catsLiveData.value = Success(it) }
    }

    private fun getFact() {

        getFactDisposable = catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .repeatWhen { completed ->
                completed.delay(5, TimeUnit.SECONDS)
            }
            .doOnError { ex -> _catsLiveData.value = Error(ex.localizedMessage ?: "Unknown error") }
            .retryWhen { errorObservable ->
                errorObservable.delay(5, TimeUnit.SECONDS)
            }
            .subscribe { _catsLiveData.value = Success(it) }
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