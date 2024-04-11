package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context,
) : ViewModel() {
    private val subscriptions = CompositeDisposable()

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
        catsService.getCatFact()
            .doOnSubscribe {
                subscriptions.add(it)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { data: Fact ->
                    _catsLiveData.value = Success(data)
                },
                { error: Throwable ->
                    _catsLiveData.value = Error(
                        error.message ?: context.getString(
                            R.string.default_error_text
                        )
                    )
                },
            )
    }

    override fun onCleared() {
        super.onCleared()
        subscriptions.dispose()
    }

    fun getFacts(): Observable<Fact> {
        return Observable.interval(2, TimeUnit.SECONDS)
            .flatMap<Fact?> {
                catsService.getCatFact()
                    .subscribeOn(Schedulers.io())
                    .toObservable()
            }
            .onErrorResumeNext(
                localCatFactsGenerator.generateCatFact()
                    .subscribeOn(Schedulers.computation())
                    .toObservable()
            )
            .observeOn(AndroidSchedulers.mainThread())
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()