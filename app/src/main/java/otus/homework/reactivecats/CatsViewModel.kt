package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _subscriptions = CompositeDisposable()
    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
        catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { fact -> emitResult(Success(fact)) },
                { error ->
                    when (error is HttpException) {
                        true -> emitResult(handleHttpError(error, context))
                        else -> emitResult(ServerError)
                    }
                }
            ).addToSubscriptions()
    }

    private fun emitResult(result: Result) {
        _catsLiveData.postValue(result)
    }

    private fun handleHttpError(error: HttpException, context: Context): Result {
        val errorBody = error.response()?.errorBody()?.string()
        return Error(errorBody ?: context.getString(R.string.default_error_text))
    }

    override fun onCleared() {
        super.onCleared()
        _subscriptions.clear()
    }

    fun getFacts() {
        catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .onErrorResumeNext { localCatFactsGenerator.generateCatFact() }
            .delay(2, TimeUnit.SECONDS)
            .retry(3)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { fact -> emitResult(Success(fact)) },
                { emitResult(ServerError) }
            ).addToSubscriptions()
    }

    private fun Disposable.addToSubscriptions() = _subscriptions.add(this)
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