package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
) : ViewModel() {

    private val _catsLiveData = BehaviorSubject.create<Result>()
    val catsLiveData: Observable<Result> = _catsLiveData
        .observeOn(AndroidSchedulers.mainThread())

    private val initSubscription: Disposable = catsService.getCatFact()
        .map<Result> { Success(it) }
        .onErrorReturn { ServerError }
        .subscribe(Consumer { _catsLiveData.onNext(it) })
    private var timerSubscription: Disposable? = null

    fun getFacts() {
        if (timerSubscription != null) return
        timerSubscription = Flowable.interval(2000, TimeUnit.MILLISECONDS)
            .concatMapSingle {
                return@concatMapSingle catsService.getCatFact()
                    .onErrorResumeNext { localCatFactsGenerator.generateCatFact() }
            }
            .map<Result> { Success(it) }
            .subscribe { _catsLiveData.onNext(it) }
    }

    override fun onCleared() {
        super.onCleared()
        initSubscription.dispose()
        timerSubscription?.dispose()
        _catsLiveData.onComplete()
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()