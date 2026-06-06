package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val compositeDisposable = CompositeDisposable()

    init {
        getFacts()
    }

    fun getFacts() {
        val disposable: Disposable = Flowable
            .interval(2, TimeUnit.SECONDS)
            .flatMapSingle {
                catsService
                    .getCatFact()
                    .onErrorResumeNext(
                        localCatFactsGenerator.generateCatFact()
                    )
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { response -> _catsLiveData.value = Success(response) },
                { throwable ->
                    when (throwable) {
                        is UnknownHostException, is ConnectException, is SocketException ->
                            _catsLiveData.value = ServerError

                        is HttpException -> _catsLiveData.value = Error("${throwable.code()}")

                        else -> _catsLiveData.value = Error(
                            throwable.message
                                ?: context.getString(R.string.default_error_text)
                        )
                    }
                }
            )
        compositeDisposable.add(disposable)
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
) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()