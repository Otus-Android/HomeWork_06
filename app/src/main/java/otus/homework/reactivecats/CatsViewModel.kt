package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class CatsViewModel(
    private val networkRepository: NetworkRepository,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val compositeDisposable = CompositeDisposable()

    init {
        /**
         * choose some variant
         */
//        getOneFact()
        getFacts()
//        generateCatFacts()
//        generateCatFactsPeriodically()
    }

    private fun getOneFact() {
        compositeDisposable.add(
            networkRepository.getCatFacts()
                .applySingleSchedulers()
                .subscribe(
                    {
                        if (it != null) _catsLiveData.value = Success(it)
                        else _catsLiveData.value =
                            Error(context.getString(R.string.default_error_text))
                    },
                    { _catsLiveData.value = ServerError }
                )
        )
    }

    private fun getFacts() {
        compositeDisposable.add(
            Observable
                .interval(TWO, TimeUnit.SECONDS)
                .flatMap {
                    networkRepository.getCatFacts().toObservable()
                }
//                .onErrorResumeNext(
//                    localCatFactsGenerator.generateCatFact().toObservable()
//                )
                .onErrorReturn {
                    localCatFactsGenerator.generateCatFact2()
                }
                .repeat()
                .applyObservableSchedulers()
                .subscribe {
                    if (it != null) _catsLiveData.value = Success(it)
                    else _catsLiveData.value = Error(context.getString(R.string.default_error_text))
                }
        )
    }

    private fun generateCatFacts(){
        compositeDisposable.add(
            localCatFactsGenerator.generateCatFact()
                .applySingleSchedulers()
                .subscribe(
                    { _catsLiveData.value = Success(it) },
                    { _catsLiveData.value = Error(it.message.toString()) }
                )
        )
    }

    private fun generateCatFactsPeriodically(){
        val catFacts = context.resources.getStringArray(R.array.local_cat_facts)
        compositeDisposable.add(
//            localCatFactsGenerator.generateCatFactPeriodically()
            Flowable
                .interval(2_000, TimeUnit.MILLISECONDS)
                .flatMap {
                    Flowable.just(Fact(catFacts[Random.nextInt(catFacts.size)]))
                }
                .distinctUntilChanged()
                .applyFlowableSchedulers()
                .subscribe(
                    { _catsLiveData.value = Success(it) },
                    { _catsLiveData.value = Error(it.message.toString()) }
                )
        )
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    companion object {
        private const val TWO = 2L
    }
}

class CatsViewModelFactory(
    private val networkRepository: NetworkRepository,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        CatsViewModel(networkRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()