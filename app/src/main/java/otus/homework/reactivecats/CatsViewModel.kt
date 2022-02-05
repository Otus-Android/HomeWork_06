package otus.homework.reactivecats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import otus.homework.coroutines.IResourceProvider
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val resources: IResourceProvider
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val disposable = CompositeDisposable()

    fun getFacts() {
        Observable.interval(5, TimeUnit.SECONDS)
            .flatMapSingle { catsService.getCatFact() }
            .onErrorResumeNext { _: Throwable -> localCatFactsGenerator.generateCatFact().toObservable() }
            .distinctUntilChanged()
            .repeatWhen { it.delay(5, TimeUnit.SECONDS) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ fact ->
                _catsLiveData.value = Success(fact)
            }, {
                _catsLiveData.value = Error(
                    message = it.message ?: resources.getString(R.string.default_error_text)
                )
            })
            .addTo(disposable)
    }

    override fun onCleared() {
        disposable.dispose()
        super.onCleared()
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val resources: IResourceProvider
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CatsViewModel(
            catsService = catsRepository,
            localCatFactsGenerator = localCatFactsGenerator,
            resources = resources
        ) as T
    }
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()