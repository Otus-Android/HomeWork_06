package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val subscribe: Disposable
    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
        subscribe = catsService.getCatFact() // 2 issue
//        subscribe = localCatFactsGenerator.generateCatFact() // 3 issue
//        subscribe = localCatFactsGenerator.generateCatFactPeriodically() // 4 issue
//        subscribe = getFacts() // 5 issue
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
            {
                _catsLiveData.postValue(Success(it))
            },
            {
                _catsLiveData.postValue(
                    Error(it.message ?: context.getString(R.string.default_error_text))
                )
            }
        )

        getFacts()
    }

    private fun getFacts(): Flowable<Fact> = catsService.getCatFact()
        .zipWith(Observable.interval(2, TimeUnit.SECONDS).blockingNext(), { i, _ -> i })
        .onErrorReturn { localCatFactsGenerator.generateCatFact().blockingGet() }
        .repeat()

    fun detachView() {
        subscribe.dispose()
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