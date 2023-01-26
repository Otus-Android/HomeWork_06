package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private  val catsService: CatsService,
    private  val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private var factSubscription: Disposable? = null
    private var factPeriodicallySubscription: Disposable? = null

    init {
        factSubscription = catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    _catsLiveData.value = Success(it)
                }, {
                    _catsLiveData.value = Error(it.message ?: "Unknown error")
                }
            )

        /*local periodically test*/

        //factSubscription = localCatFactsGenerator.generateCatFactPeriodically().subscribe(
        //    {
        //        _catsLiveData.value = Success(it)
        //   }, {
        //       _catsLiveData.value = Error(it.message ?: "Unknown error")
        //   }
        //)

        /*local periodically test*/

        /*network periodically test*/
        getFacts()
        /*network periodically test*/

    }

    private fun getFacts() {

        factPeriodicallySubscription = Observable
            .interval(2, TimeUnit.SECONDS).flatMap {
                return@flatMap catsService.getCatFact().toObservable().onErrorResumeNext { _: Throwable ->
                    localCatFactsGenerator.generateCatFact().toObservable()
                }
            }
        //.debounce(2000, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            {
               _catsLiveData.value = Success(it)
            }, {
                _catsLiveData.value = Error(it.message ?: "Unknown error")
            }
        )

    }


    override fun onCleared() {
        super.onCleared()
        factSubscription?.dispose()
        factPeriodicallySubscription?.dispose()
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