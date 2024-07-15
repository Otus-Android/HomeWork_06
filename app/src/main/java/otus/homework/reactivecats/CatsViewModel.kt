package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    val localCatFactsGenerator: LocalCatFactsGenerator,
    val context: Context,
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val compositeDisposable = CompositeDisposable()

    init {
//        getCatFact()
//        generateRandomCatFacts()
//        generateCatFactPeriodically()
        getFacts()
    }

    private fun getCatFact() {
        val disposable = catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { response ->
                    setResult(response)
                },
                { t ->
                    _catsLiveData.value = ServerError
                }
            )
        compositeDisposable.add(disposable)
    }

    private fun generateRandomCatFacts() {
        val disposable = localCatFactsGenerator.generateCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { response ->
                    setResult(response)
                },
                { t ->
                    _catsLiveData.value = ServerError
                }
            )

        compositeDisposable.add(disposable)
    }

    private fun generateCatFactPeriodically() {
        val disposable = localCatFactsGenerator.generateCatFactPeriodically()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe(
                { response ->
                    setResult(response)
                },
                { t ->
                    _catsLiveData.value = ServerError
                }
            )
        compositeDisposable.add(disposable)
    }

    private fun getFacts() {
        val disposable = Flowable.interval(2000, TimeUnit.MILLISECONDS)
            .flatMapSingle { catsService.getCatFact() }
            .onErrorResumeNext(localCatFactsGenerator.generateCatFactPeriodically())
            .subscribe(
                { response ->
                    setResult(response)
                },
                { t ->
                    _catsLiveData.value = ServerError
                }
            )
        compositeDisposable.add(disposable)
    }

    private fun setResult(fact: Fact?) {
        if (fact != null) {
            _catsLiveData.value = Success(fact)
        } else {
            _catsLiveData.value = Error(
                context.getString(R.string.default_error_text)
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
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