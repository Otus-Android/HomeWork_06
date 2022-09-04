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
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val compositeDisposable = CompositeDisposable()

    init {
        val catFactDisposable = catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe (
                { response ->
                    _catsLiveData.value = Success(response)
                },
                {
                    _catsLiveData.value = Error(
                        it.message ?: context.getString(
                            R.string.default_error_text
                        )
                    )
                }
            )

        compositeDisposable.add(catFactDisposable)


        getFacts()
    }

    private fun getFacts() {
        val disposable = Observable.interval(2000, TimeUnit.MILLISECONDS)
            .flatMap {
                catsService.getCatFact()
                    .map { response ->
                        Success(response)
                    }
                    .onErrorReturn {
                        Success(localCatFactsGenerator.generateCatFact().blockingGet())
                    }
                    .toObservable()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe (
                { result ->
                    _catsLiveData.value = result
                },
                {
                    _catsLiveData.value = ServerError
                }
            )

        /*//Проверка метода generateCatFactPeriodically
        val disposable = localCatFactsGenerator.generateCatFactPeriodically()
            .map {
                Success(it)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe (
                { result ->
                    _catsLiveData.value = result
                },
                {
                    _catsLiveData.value = ServerError
                }
            )*/

        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
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