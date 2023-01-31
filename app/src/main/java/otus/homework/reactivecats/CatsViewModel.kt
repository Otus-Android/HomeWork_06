package otus.homework.reactivecats

import androidx.lifecycle.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val compositeDisposable = CompositeDisposable()

    init {
        /*compositeDisposable.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _catsLiveData.value = Success(it)
                    },
                    {
                        _catsLiveData.value = it.parseException()
                    }
                )
        )

        compositeDisposable.add(
            localCatFactsGenerator.generateCatFact()
                .subscribeOn(Schedulers.io())
                .delay(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _catsLiveData.value = Success(it)
                },
                    {
                        _catsLiveData.value = it.parseException()
                    })
        )

        compositeDisposable.add(
            localCatFactsGenerator.generateCatFactPeriodically()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _catsLiveData.value = Success(it)
                },
                    {
                        _catsLiveData.value = it.parseException()
                    })
        )*/
        getFacts()
    }

    private fun getFacts() {
        compositeDisposable.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .onErrorResumeNext(localCatFactsGenerator.generateCatFact())
                .delay(2, TimeUnit.SECONDS)
                .repeat()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _catsLiveData.value = Success(it)
                    },
                    {
                        _catsLiveData.value = it.parseException()
                    }
                )
        )
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}

fun Throwable.parseException(): Result {
    return when (this) {
        is IOException -> ServerError
        else -> Error(this.message)
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
data class Error(val message: String?) : Result()
object ServerError : Result()