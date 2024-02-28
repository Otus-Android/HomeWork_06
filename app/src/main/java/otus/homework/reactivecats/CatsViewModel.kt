package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

    private val compositeDisposable = CompositeDisposable()

    init {
        //val rx = catsService.getCatFact()
        //val rx = localCatFactsGenerator.generateCatFact()
        val rx = localCatFactsGenerator.generateCatFactPeriodically()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> _catsLiveData.value = Success(result) },
                { error -> _catsLiveData.value = Error(error.message.toString()) })
        compositeDisposable.add(rx)
       //getFacts()
    }

    private fun getFacts() {
        val rx = catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .onErrorResumeNext(localCatFactsGenerator.generateCatFact())
            .repeatWhen { completed -> completed.delay(2000, TimeUnit.MILLISECONDS) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> _catsLiveData.value = Success(result) },
                { error -> _catsLiveData.value = Error(error.message.toString()) })
        compositeDisposable.add(rx)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
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
data object ServerError : Result()