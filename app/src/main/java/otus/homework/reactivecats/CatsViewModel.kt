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
    val catsService: CatsService,
    val localCatFactsGenerator: LocalCatFactsGenerator,
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val disposables = CompositeDisposable()

    init {
        getFacts()
    }

    fun getFacts() {
        disposables.add(
            Flowable.interval(2, TimeUnit.SECONDS)
                .flatMapSingle {
                    catsService.getCatFact()
                        .onErrorResumeNext { localCatFactsGenerator.generateCatFact() }
                }
                .distinctUntilChanged()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _catsLiveData.value = Success(it)
                    },
                    { exception ->
                        _catsLiveData.value = exception.message?.let { Error(it) } ?: ServerError
                    },
                )
        )

    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}

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
object ServerError : Result()