package otus.homework.reactivecats

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

@SuppressLint("StaticFieldLeak")
class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val compositeDisposable = CompositeDisposable()

    init {
        val disposable = catsService.getCatFact()
            .applyScheduler()
            .subscribe(::handleSuccess, ::handleError)
        compositeDisposable.add(disposable)
    }

    private fun handleError(error: Throwable) {
        Log.e(this::class.simpleName, "handleError: ", error)

        _catsLiveData.value = when (error) {
            is HttpException -> ServerError
            else -> Error(
                error.message ?: context.getString(
                    R.string.default_error_text
                )
            )
        }
    }

    private fun handleSuccess(fact: Fact) {
        _catsLiveData.value = Success(fact)
    }

    fun getFacts() {
        val disposable = Observable.interval(2, 2, TimeUnit.SECONDS)
            .flatMapSingle {
                catsService.getCatFact()
                    .onErrorResumeNext { localCatFactsGenerator.generateCatFact() }
            }
            .applyScheduler()
            .subscribe(::handleSuccess, ::handleError)
        compositeDisposable.add(disposable)
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
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()