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

class CatsViewModel(
    service: CatsService,
    localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val defaultErrorMessage = context.getString(R.string.default_error_text)

    init {
        service.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                this::onSuccess,
                this::onError
            ).also(this::addToCompositeDisposables)
    }

    private fun addToCompositeDisposables(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        compositeDisposable.clear()
    }

    private fun onSuccess(fact: Fact) {
        _catsLiveData.value = Success(fact)
    }

    private fun onError(throwable: Throwable) =
        if (throwable is java.net.SocketTimeoutException) {
            _catsLiveData.value = ServerError
        } else {
            _catsLiveData.value = Error(throwable.message ?: defaultErrorMessage)
        }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()