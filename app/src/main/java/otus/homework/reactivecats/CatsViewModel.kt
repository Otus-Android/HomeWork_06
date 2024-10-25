package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private lateinit var disposable: Disposable
    private val defaultError = context.getString(R.string.default_error_text)

    init {
        getFacts()
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }

    private fun getFacts() {
        disposable = catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorResumeNext { localCatFactsGenerator.generateCatFact() }
            .repeatWhen { it.delay(2, TimeUnit.SECONDS) }
            .subscribe(
                { fact -> _catsLiveData.value = Success(fact) },
                { throwable ->
                    if (isNetworkTypeException(throwable)) _catsLiveData.value = NetworkError
                    else _catsLiveData.value = Error(throwable.message ?: defaultError)
                }
            )
    }

    private fun <T> isNetworkTypeException(t: T): Boolean {
        return t is SocketException ||
                t is SocketTimeoutException ||
                t is UnknownHostException
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
object NetworkError : Result()