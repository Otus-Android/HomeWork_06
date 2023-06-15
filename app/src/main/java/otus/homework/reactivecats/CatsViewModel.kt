package otus.homework.reactivecats

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CatsViewModel(
    private val repository: CatsRepository,
    context: Context
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val defaultErrorMessage = context.getString(R.string.default_error_text)
    private var refreshJob: Job? = null

    init {
        viewModelScope.launch {
            while (true) {
                delay(2000)
                refreshJob?.cancelAndJoin()
                refreshJob = null
                refreshJob = launch {
                    getFacts()
                }
            }
        }
    }

    fun getFacts() {
        repository.getFacts()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                this::onSuccess,
                this::onFactError
            ).also(this::addToCompositeDisposables)
    }

    private fun onFactError(throwable: Throwable) {
        repository.getLocalFactsPeriodically()
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

    private fun onSuccess(fact: Fact) {
        _catsLiveData.value = Success(fact)
    }

    private fun onError(throwable: Throwable) {
        if (throwable is java.net.SocketTimeoutException) {
            _catsLiveData.value = ServerError
        } else {
            _catsLiveData.value = Error(throwable.message ?: defaultErrorMessage)
        }
        Log.w(CatsViewModel::class.java.toString(), throwable.message ?: "", throwable)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        compositeDisposable.clear()
        refreshJob?.cancel()
        refreshJob = null
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsRepository,
    private val context: Context
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()