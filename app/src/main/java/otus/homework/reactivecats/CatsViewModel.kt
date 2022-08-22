package otus.homework.reactivecats

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val _showProgressBar = MutableLiveData<Boolean>()
    val showProgressBar: LiveData<Boolean> = _showProgressBar
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    init {
        val disposable = Single.fromCallable { catsService.getCatFact().execute() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                _showProgressBar.value = true
            }
            .doAfterTerminate {
                _showProgressBar.value = false
                getFacts(context)
            }
            .subscribe(
                { response ->
                    if (response.isSuccessful) {
                        _catsLiveData.value = response.body()?.let { Success(it) }
                    } else {
                        _catsLiveData.value = Error(
                            context.getString(R.string.default_error_text)
                        )
                    }
                },
                {
                    _catsLiveData.value = ServerError
                }
            )
        compositeDisposable.add(disposable)
    }

    private fun getFacts(context: Context) {
        val disposable = Observable.interval(0L, 2L, TimeUnit.SECONDS)
            .map {
                Single.fromCallable { catsService.getCatFact().execute() }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe {
                        _showProgressBar.postValue(true)
                    }
                    .doAfterTerminate {
                        _showProgressBar.postValue(false)
                    }
                    .subscribe({ response ->
                        if (response.isSuccessful) {
                            _catsLiveData.value = response.body()?.let { Success(it) }
                        } else {
                            getFromFactsGenerator(context)
                        }
                    }, {
                        getFromFactsGenerator(context)
                    }
                    )
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()

        compositeDisposable.add(disposable)
    }

    @SuppressLint("CheckResult")
    private fun getFromFactsGenerator(context: Context) {
        localCatFactsGenerator.generateCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _catsLiveData.value = Success(it)
            },
                {
                    context.getString(R.string.default_error_text)
                }
            )
    }

    override fun onCleared() {
        super.onCleared()
        if (compositeDisposable.isDisposed.not()) {
            compositeDisposable.dispose()
        }
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