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
import java.util.concurrent.TimeUnit.SECONDS
import otus.homework.reactivecats.R.string
import retrofit2.HttpException

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context,
) : ViewModel() {

    private var onClearedCompositDisposable = CompositeDisposable()
    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
        catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                ::handleGetCatFactSuccess,
                ::handlGetFactError
            ).also(onClearedCompositDisposable::add)
    }

    fun getFacts() {
        Observable.interval(0, 2, SECONDS, Schedulers.io())
            .flatMap {
                catsService.getCatFact()
                    .onErrorResumeNext(localCatFactsGenerator.generateCatFact())
                    .toObservable()
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                ::handleGetCatFactSuccess,
                ::handlGetFactError
            )
            .also(onClearedCompositDisposable::add)
    }

    private fun handleGetCatFactSuccess(catFact : Fact) {
        _catsLiveData.value = Success(catFact)
    }

    private fun handlGetFactError(throwable : Throwable?) {
        _catsLiveData.value = when (throwable) {
            is HttpException -> {
                Error(
                    throwable
                        .response()
                        ?.errorBody()
                        ?.string() ?: context.getString(
                        string.default_error_text
                    )
                )
            }

            else -> ServerError
        }
    }

    override fun onCleared() {
        onClearedCompositDisposable.dispose()
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