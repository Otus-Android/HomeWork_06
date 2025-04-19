package otus.homework.reactivecats

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
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
        getFacts()
    }

    private fun onCatsServiceSuccess(fact: Fact) {
        _catsLiveData.value = Success(fact)
    }

    private fun onCatsServiceError(error: Throwable) {
        _catsLiveData.value = if (error is HttpException) {
            ServerError
        } else {
            Error((error.message ?: R.string.default_error_text).toString())
        }
    }

    private fun getFacts() {
        compositeDisposable.add(Observable
            .interval(2, TimeUnit.SECONDS)
            .map { getSingleFact() }
            .distinct()
            .subscribe { })
    }

    private fun getSingleFact() {
        compositeDisposable.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext {
                    localCatFactsGenerator.generateCatFact()
                }
                .subscribe(
                    { fact -> onCatsServiceSuccess(fact) },
                    { error -> onCatsServiceError(error) })
        )
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}


class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()