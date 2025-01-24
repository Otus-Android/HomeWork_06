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
    private val disposable = CompositeDisposable()

    init {
        disposable.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _catsLiveData.value = Success(it)
                    },
                    {
                        _catsLiveData.value = Error(message = it.message ?: "Some error")
                    }
                )
        )

        disposable.add(
            localCatFactsGenerator.generateCatFactPeriodically()
                .subscribe({
                    println("Fact = $it\n")
                })
        )

        getFacts()
    }

    fun getFacts() {
        disposable.add(
            Observable.interval(2, TimeUnit.SECONDS).doOnNext {
                //скорее всего есть какой-то более подходящий вариант,
                // поскольку текущая реализация не выглядит "элегантной"
                disposable.add(
                    catsService.getCatFact()
                        .onErrorResumeNext(localCatFactsGenerator.generateCatFact())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            _catsLiveData.value = Success(it)
                        }, {
                            _catsLiveData.value = Error(message = it.message ?: "Some error")
                        })
                )
            }.subscribe()
        )
    }

    override fun onCleared() {
        disposable.dispose()
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