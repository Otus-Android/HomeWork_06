package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException

class CatsViewModel(
    private val catsRepository: CatsRepository
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val subscriptions = CompositeDisposable()

    init {
        getFacts()
    }

    private fun getFacts() {
        val catsSubscription = catsRepository.getFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { fact -> _catsLiveData.value = Success(fact) },
                { error ->
                    when (error) {
                        is SocketTimeoutException -> _catsLiveData.value = ServerError
                        else -> _catsLiveData.value = Error(error.message ?: "")
                    }
                }
            )


        subscriptions.addAll(catsSubscription)
    }

    override fun onCleared() {
        super.onCleared()
        subscriptions.dispose()
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsRepository
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()
