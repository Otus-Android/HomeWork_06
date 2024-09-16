package otus.homework.reactivecats

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
        getFacts()
    }

    private fun getFact() {
        compositeDisposable.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { facts ->
                        if (facts.isNotEmpty())
                            _catsLiveData.value = Success(facts.first())
                        else
                            _catsLiveData.value = Error(
                                context.getString(
                                    R.string.default_error_text
                                )
                            )
                    },
                    {
                        _catsLiveData.value = Error(
                            it.message ?: context.getString(
                                R.string.default_error_text
                            )
                        )
                        Log.d("CatsVMLogs", it.message.toString())
                    }
                )
        )
    }

    fun getFacts() {
        val disposable =
            Observable.interval(2, TimeUnit.SECONDS)
                .flatMap {
                    catsService.getCatFact()
                        .onErrorResumeNext { localCatFactsGenerator.generateCatFact() }
                        .toObservable()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { facts ->
                    if (facts.isNotEmpty())
                        _catsLiveData.value = Success(facts.first())
                    else
                        _catsLiveData.value = Error(
                            context.getString(
                                R.string.default_error_text
                            )
                        )
                }
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
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