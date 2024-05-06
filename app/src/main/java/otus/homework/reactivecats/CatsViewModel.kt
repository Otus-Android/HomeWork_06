package otus.homework.reactivecats

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val compositeDisposable = CompositeDisposable()
    private val defaultErrorMessage = context.getString(R.string.default_error_text)

    init {
        getFacts()
//        getCatFact()
//        getCatFactPeriodically()
    }

    private fun getFacts() {
        Log.d("MyAppRX", "getFacts()")
        compositeDisposable.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .onErrorResumeNext(localCatFactsGenerator.generateCatFactPeriodically())
                .repeatWhen { it.delay(2000, TimeUnit.MILLISECONDS) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _catsLiveData.value = Success(it)
                    Log.d("MyAppRX", "getFacts() ${it.text}")
                },
                    { error -> _catsLiveData.setValue(errorParser(error)) })
        )
    }

    private fun getCatFact() {
        Log.d("MyAppRX", "getCatFact()")
        compositeDisposable.add(
            localCatFactsGenerator.generateCatFact()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ _catsLiveData.value = Success(it) },
                    { error -> _catsLiveData.setValue(errorParser(error)) })
        )
    }

    private fun getCatFactPeriodically() {
        Log.d("MyAppRX", "getCatFactPeriodically()")
        compositeDisposable.addAll(
            localCatFactsGenerator.generateCatFactPeriodically()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _catsLiveData.value = Success(it)
                    Log.d("MyAppRX", "getCatFactPeriodically() ${it.text}")
                },
                    { error -> _catsLiveData.setValue(errorParser(error)) })
        )
    }


    private fun errorParser(error: Throwable?): Result {
        return when (error) {
            is HttpException -> {
                Log.e("MyAppRX", "HttpException")
                Error(error.message())
            }

            is IOException -> {
                Log.e("MyAppRX", "IOException")
                ServerError
            }

            else -> {
                Log.e("MyAppRX", defaultErrorMessage)
                Error(
                    error?.message ?: defaultErrorMessage
                )
            }
        }
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