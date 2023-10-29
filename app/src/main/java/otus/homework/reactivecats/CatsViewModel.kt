package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val defaultErrorText: String by lazy {
        context.getString(R.string.default_error_text)
    }

    private val compositeDisposable = CompositeDisposable()

    init {
        getFacts(localCatFactsGenerator)
//        getGeneratedFact(localCatFactsGenerator)
//        getPeriodicallyGeneratedFact(localCatFactsGenerator)
    }

    private fun getFacts(localCatFactsGenerator: LocalCatFactsGenerator) {
        val disposable = catsService.getCatFact()
            .onErrorResumeNext { localCatFactsGenerator.generateCatFact() }
            .subscribeOn(Schedulers.io())
            .delay(DELAY_TIME, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .repeat()
            .subscribe(
                { fact ->
                    if (fact != null) {
                        _catsLiveData.value = Success(fact)
                    } else {
                        _catsLiveData.value = Error(defaultErrorText)
                    }
                },
                { tr ->
                    tr.printStackTrace()
                    _catsLiveData.value = ServerError
                }
            )
        compositeDisposable.add(disposable)
    }

    private fun getPeriodicallyGeneratedFact(localCatFactsGenerator: LocalCatFactsGenerator) {
        val disposable = localCatFactsGenerator.generateCatFactPeriodically()
            .observeOn(AndroidSchedulers.mainThread())
            .repeat()
            .subscribe(
                { fact ->
                    if (fact != null) {
                        _catsLiveData.value = Success(fact)
                    } else {
                        _catsLiveData.value = Error(defaultErrorText)
                    }
                },
                { tr ->
                    tr.printStackTrace()
                    _catsLiveData.value = ServerError
                }
            )
        compositeDisposable.add(disposable)
    }

    private fun getGeneratedFact(localCatFactsGenerator: LocalCatFactsGenerator) {
        val disposable = localCatFactsGenerator.generateCatFact()
            .onErrorResumeNext { localCatFactsGenerator.generateCatFact() }
            .subscribeOn(Schedulers.computation())
            .delay(DELAY_TIME, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .repeat()
            .subscribe(
                { fact ->
                    if (fact != null) {
                        _catsLiveData.value = Success(fact)
                    } else {
                        _catsLiveData.value = Error(defaultErrorText)
                    }
                },
                { tr ->
                    tr.printStackTrace()
                    _catsLiveData.value = ServerError
                }
            )
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    companion object {
        private const val DELAY_TIME = 2000L
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            CatsViewModel::class.java -> {
                CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
            }

            else -> {
                error("unknown $modelClass")
            }
        }
    }
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
data object ServerError : Result()