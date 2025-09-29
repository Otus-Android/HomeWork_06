package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLHandshakeException


class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val disposables = CompositeDisposable()

    init {
        val disposable: Disposable = getFacts()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { res ->
                    _catsLiveData.value = Success(res)
                }, { error ->
                    _catsLiveData.value = Error(
                        error.message ?: context.getString(
                            R.string.default_error_text
                        )
                    )
                }
            )
        disposables.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    fun getFacts(): Flowable<Fact> {
        return Flowable.interval(0L, 2000L, TimeUnit.MILLISECONDS)
            .flatMapSingle {
                catsService.getCatFact()
                    .onErrorResumeNext { error ->
                        // Добавил проверку ошибки "сетевая ли?" согласно пункту 5.2. Возможно, я не так понял.
                        if (error.isNetworkException()) {
                            localCatFactsGenerator.generateCatFact()
                        } else {
                            Single.just(Fact(""))
                        }
                    }
            }
            .distinctUntilChanged { prev, cur -> prev.fact == cur.fact }
    }

    private fun Throwable.isNetworkException(): Boolean {
        val networkErrors = listOf(
            UnknownHostException::class,
            SocketTimeoutException::class,
            SSLHandshakeException::class,
            ConnectException::class,
            HttpException::class
        )
        return networkErrors.contains(this::class)
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
