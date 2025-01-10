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
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private var disposable: CompositeDisposable = CompositeDisposable()
    private val defErrorText = context.getString(R.string.default_error_text)

    init {
        getFacts()
    }

    private fun getFacts() {
        val dispose = Observable.interval(200L, TimeUnit.MILLISECONDS, Schedulers.computation())
            .subscribeOn(Schedulers.io())
            .flatMap {
                catsService.getCatFact()
                    .onErrorResumeNext {
                        localCatFactsGenerator.generateCatFact()
                    }.toObservable()

            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { catFact ->
                    _catsLiveData.value = Success(catFact)
                },
                { throwable ->
                    _catsLiveData.value = Error(throwable.message ?: defErrorText)
                })
        disposable.add(dispose)
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
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