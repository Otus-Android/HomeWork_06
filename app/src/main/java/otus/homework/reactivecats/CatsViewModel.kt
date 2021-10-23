package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context,
    mainScheduler: Scheduler = AndroidSchedulers.mainThread(),
    ioScheduler: Scheduler = Schedulers.io()
) : ViewModel() {
    
    private val disposables = CompositeDisposable()
    
    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
        val factDisposable = getFacts()
            .subscribeOn(ioScheduler)
            .observeOn(mainScheduler)
            .subscribe(
                { fact ->
                    _catsLiveData.value = Success(fact)
                },
                { e ->
                    _catsLiveData.value = handleError(e, context)
                }
            )
        disposables.add(factDisposable)
    }
    
    private fun handleError(e: Throwable, context: Context): Result {
        return if (e is HttpException) {
            Error(e.response()?.errorBody()?.string() ?: context.getString(R.string.default_error_text))
        } else {
            ServerError
        }
    }
    
    private fun getFacts(): Flowable<Fact> {
        return Flowable.interval(CAT_FACT_FETCH_INTERVAL, TimeUnit.MILLISECONDS)
            .switchMap {
                catsService.getCatFact()
                    .toFlowable()
                    .onErrorResumeNext(localCatFactsGenerator.generateCatFact().toFlowable())
            }
    }
    
    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
    
    companion object {
        private const val CAT_FACT_FETCH_INTERVAL = 2_000L
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