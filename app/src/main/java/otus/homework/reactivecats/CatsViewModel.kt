package otus.homework.reactivecats

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.operators.flowable.FlowableIgnoreElements
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class CatsViewModel(
    catsService: CatsService,
    localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val dispose = object: DisposableSubscriber<Fact>() {

        @SuppressLint("CheckResult")
        override fun onError(e: Throwable) {
            localCatFactsGenerator.generateCatFact().subscribe { it -> _catsLiveData.value = Success(it) }
        }

        override fun onComplete() {
            _catsLiveData.value = Success(Fact("That's all!!"))
        }

        override fun onNext(t: Fact?) {
            _catsLiveData.value = t?.let { Success(it) }
        }
    }

    init {

        getFacts(catsService)

    }

    override fun onCleared() {
        dispose.dispose()
        super.onCleared()
    }

    @SuppressLint("CheckResult")
    fun getFacts(catsService: CatsService
    ) {

        Flowable.create<Fact>( { fact ->

            Flowable
                .interval(2, TimeUnit.SECONDS)
                .subscribe {
                    catsService.getCatFact().subscribe(){
                        fact.onNext(it)
                    }
                }
        }, BackpressureStrategy.LATEST)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(dispose)

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