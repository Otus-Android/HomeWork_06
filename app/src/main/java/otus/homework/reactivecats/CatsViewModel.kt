package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subscribers.DisposableSubscriber
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val subscriber: DisposableSubscriber<Fact>

    init {
        subscriber = getFacts()
    }

    override fun onCleared() {
        super.onCleared()
        subscriber.dispose()
    }

    fun getFacts(): DisposableSubscriber<Fact> =
        Flowable.interval(2, TimeUnit.SECONDS).flatMapSingle({
            catsService.getCatFact()
        }, false, 1).onErrorResumeWith(localCatFactsGenerator.generateCatFactPeriodically())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(getCatObserver())

    private fun getCatObserver(): DisposableSubscriber<Fact> =
        object : DisposableSubscriber<Fact>() {
            override fun onNext(t: Fact) {
                _catsLiveData.value = Success(t)
                request(1)

            }

            override fun onError(e: Throwable) {
                _catsLiveData.value = ServerError
            }

            override fun onComplete() {
                _catsLiveData.value = Success(Fact("Complete"))
            }

            override fun onStart() {
                request(1)
            }

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