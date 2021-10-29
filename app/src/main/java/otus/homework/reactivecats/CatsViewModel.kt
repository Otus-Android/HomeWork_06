package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers.io
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit


class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private var compositeDisposable = CompositeDisposable()

    init {
           getFacts()
    }

    private fun onFailure(t: Throwable) {
        when (t) {
            is SocketTimeoutException -> _catsLiveData.value = ServerError
            else ->  _catsLiveData.value = Error(t.message ?: "Ошибка")
        }
    }

    private fun onResponse(response: Fact) {
        _catsLiveData.value = Success(response)
    }

    private fun getFact(){
        compositeDisposable.add(
            catsService.getCatFact()
                .subscribeOn(io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {response -> onResponse(response)},
                    {t -> onFailure(t) }))
    }

     private fun getFacts() {
         compositeDisposable.add(
             Observable
            .interval(2000, TimeUnit.MILLISECONDS)
            .flatMap { catsService.getCatFact().onErrorResumeNext {
                localCatFactsGenerator.generateCatFact() }.toObservable() }
            .subscribeOn(io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe (
                {response -> onResponse(response)},
                {t -> onFailure(t) }))
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