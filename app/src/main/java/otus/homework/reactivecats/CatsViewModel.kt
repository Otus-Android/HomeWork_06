package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val errorMessage
        get() = context.resources.getString(R.string.default_error_text)

    private val compositeDisposable = CompositeDisposable()

    init {
        getFacts()
    }

    private fun generateCatFactPeriodically() {
        compositeDisposable.add(
            localCatFactsGenerator
                .generateCatFactPeriodically()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ fact ->
                    _catsLiveData.value = Success(fact)
                }, {
                    when (it) {
                        is IOException -> _catsLiveData.value = ServerError
                        else -> _catsLiveData.value = Error(it.message ?: errorMessage)
                    }
                }
                )
        )
    }

    private fun generateCatFact() {
        compositeDisposable.add(
            localCatFactsGenerator
                .generateCatFact()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ fact ->
                    _catsLiveData.value = Success(fact)
                }, {
                    when (it) {
                        is IOException -> _catsLiveData.value = ServerError
                        else -> _catsLiveData.value = Error(it.message ?: errorMessage)
                    }
                }
                )
        )
    }

    private fun getFact() {
        compositeDisposable.add(
            catsService
                .getCatFact()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ fact ->
                    _catsLiveData.value = Success(fact)
                }, {
                    when (it) {
                        is IOException -> _catsLiveData.value = ServerError
                        else -> _catsLiveData.value = Error(it.message ?: errorMessage)
                    }
                })
        )
    }

    private fun getFacts() {

        var i = 0
        compositeDisposable.add(
            catsService.getCatFact()
                .onErrorResumeNext {
                    println(i++)
                    localCatFactsGenerator.generateCatFact().firstOrError()
                }
                .repeatWhen {
                    it.delay(2, TimeUnit.SECONDS)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .distinctUntilChanged()
                .subscribe({
                    val success = Success(Fact("$it $i"))
                    println(success)
                    _catsLiveData.value = Success(Fact("$it $i"))
                }, {
                    when (it) {
                        is IOException -> _catsLiveData.value = ServerError
                        else -> _catsLiveData.value = Error(it.message ?: errorMessage)
                    }
                })

        )
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
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()