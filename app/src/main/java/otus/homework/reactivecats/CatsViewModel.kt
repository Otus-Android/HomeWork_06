package otus.homework.reactivecats

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

@SuppressLint("CheckResult")
class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private var disposables = CompositeDisposable()

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
//        getFact()
        getFacts()
    }

    private fun getFact() {
        catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(disposables::add)
            .subscribe(this::onSuccess, this::onError)
    }

    private fun getFacts() {
        val d = Flowable.interval(0, 2, TimeUnit.SECONDS)
            .onBackpressureLatest()
            .flatMap { catsService.getCatFact().toFlowable() }
            .onErrorResumeNext { _: Throwable ->
                localCatFactsGenerator.generateCatFact().toFlowable()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .distinctUntilChanged()
            .subscribe(this::onSuccess, this::onError)
        disposables.add(d)
    }

    private fun onSuccess(fact: Fact) {
        _catsLiveData.value = Success(fact)
    }

    private fun onError(ex: Throwable) {
        _catsLiveData.value = if (ex is SocketTimeoutException) {
            ServerError
        } else {
            Error(ex.message ?: context.getString(R.string.default_error_text))
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()