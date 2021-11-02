package otus.homework.reactivecats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class CatsViewModel(
    catFactsRepository: CatFactsRepository,
    rxSchedulers: RxSchedulers,
    private val resourceWrapper: ResourceWrapper
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val compositeDisposable = CompositeDisposable()

    init {
        Flowable.interval(0, REPEAT_TIME, TimeUnit.SECONDS, rxSchedulers.ioScheduler)
            .flatMapSingle { catFactsRepository.getCatFact() }
            .subscribeOn(rxSchedulers.ioScheduler)
            .observeOn(rxSchedulers.mainThreadScheduler)
            .subscribe(
                { _catsLiveData.value = Success(it) },
                { _catsLiveData.value = Error(it.message ?: resourceWrapper.getString(R.string.default_error_text)) }
            )
            .also { compositeDisposable.add(it) }
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    private companion object {
        const val REPEAT_TIME = 2L
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatFactsRepository,
    private val rxSchedulers: RxSchedulers,
    private val resourceWrapper: ResourceWrapper
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, rxSchedulers, resourceWrapper) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()