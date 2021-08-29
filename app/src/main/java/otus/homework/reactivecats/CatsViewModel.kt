package otus.homework.reactivecats

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

private const val DELAY = 2000L

class CatsViewModel(
    private val service: ActivitiesService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context,
    private val mainScheduler: Scheduler,
    private val ioScheduler: Scheduler
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val compositeDisposable = CompositeDisposable()

    init {
        getOneFact(context)
    }

    private fun getOneFact(context: Context) {
        val disposable = service.getActivity()
            .subscribeOn(ioScheduler)
            .observeOn(mainScheduler)
            .subscribe(
                { _catsLiveData.value = Success(it) },
                {
                    _catsLiveData.value = Error(
                        it.message ?: context.getString(
                            R.string.default_error_text
                        )
                    )
                })
        compositeDisposable.add(disposable)
    }

    fun getMoreFacts() {
        compositeDisposable.clear()
        val disposable =
            Flowable.interval(DELAY, TimeUnit.MILLISECONDS)
                .flatMap {
                    service.getActivity()
                        .onErrorResumeNext(localCatFactsGenerator.generateCatFact())
                        .toFlowable()
                }
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribe({
                    _catsLiveData.value = Success(it)
                }, {
                    _catsLiveData.value = Error(
                        it.message ?: context.getString(
                            R.string.default_error_text
                        )
                    )
                })
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}

class CatsViewModelFactory(
    private val catsRepository: ActivitiesService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context,
    private val mainScheduler: Scheduler,
    private val ioScheduler: Scheduler
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context, mainScheduler = mainScheduler, ioScheduler = ioScheduler) as T
}

sealed class Result
data class Success(val fact: ActivityResponse) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()