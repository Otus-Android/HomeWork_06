package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
        private val catsService: CatsService,
        private val localCatFactsGenerator: LocalCatFactsGenerator,
        private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val compositeDisposable = CompositeDisposable()

    init {
        getSingleFacts()
        getFacts()
    }

    fun getSingleFacts() {
        compositeDisposable.add(
                catsService.getCatFact()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { fact ->
                                    if (fact != null) _catsLiveData.value = Success(fact)
                                    else _catsLiveData.value =
                                            Error(context.getString(R.string.default_error_text))
                                },
                                { _catsLiveData.value = ServerError }
                        )
        )
    }

    fun getFacts() {
        compositeDisposable.add(
                Observable.interval(0L,2L, TimeUnit.MILLISECONDS)
                        .subscribe {
                            catsService.getCatFact()
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            { fact ->
                                                if (fact != null) _catsLiveData.value = Success(fact)
                                                else _catsLiveData.value =
                                                        Error(context.getString(R.string.default_error_text))
                                            }, {
                                        localCatFactsGenerator.generateCatFact().map {
                                            _catsLiveData.value = Success(it)
                                        }
                                    }
                                    )
                        }
        )
    }

    fun generateCatFact() {
        compositeDisposable.add(
                localCatFactsGenerator.generateCatFact()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { _catsLiveData.value = Success(it) },
                                { _catsLiveData.value = Error(it.message.toString()) }
                        )
        )
    }

    fun generateCatFactPeriodically() {
        compositeDisposable.add(
                localCatFactsGenerator.generateCatFactPeriodically()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
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