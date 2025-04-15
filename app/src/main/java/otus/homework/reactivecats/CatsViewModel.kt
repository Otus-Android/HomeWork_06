package otus.homework.reactivecats

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.FlowableEmitter
import io.reactivex.FlowableSubscriber
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscription
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val error = context.getString(
        R.string.default_error_text
    )
    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val compositeDisposable = CompositeDisposable()

    private fun init() {
        compositeDisposable.add(
            catsService.getCatFact().toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { n ->
                        _catsLiveData.value = Success(n)
                    }, { e ->
                        if (e is HttpException) {
                            _catsLiveData.value = Error(e.message ?: error)
                        } else {
                            _catsLiveData.value = ServerError
                        }
                    }, {}, {}
                )
        )
    }

    init {
        init()
//        getFacts()
    }

    fun getFacts() {
        compositeDisposable.add(
            Observable.interval(timeout, TimeUnit.MILLISECONDS).flatMapSingle {
                catsService.getCatFact()
            }.onErrorResumeNext (
                localCatFactsGenerator.generateCatFact().toObservable()
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { n ->
                        _catsLiveData.value = Success(n)
                    }, { e ->
                        if (e is HttpException) {
                            _catsLiveData.value = Error(e.message ?: error)
                        } else {
                            _catsLiveData.value = ServerError
                        }
                    }, {}, {}
                )
        )
    }

    override fun onCleared() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
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