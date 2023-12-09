package otus.homework.reactivecats

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import otus.homework.reactivecats.LocalCatFactsGenerator.Companion.DELAY_SECOND
import java.util.concurrent.TimeUnit



@SuppressLint("StaticFieldLeak")
class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val compositeDisposable = CompositeDisposable()

    init {
//        catsService.getCatFact().enqueue(object : Callback<Fact> {
//            override fun onResponse(call: Call<Fact>, response: Response<Fact>) {
//                if (response.isSuccessful && response.body() != null) {
//                    _catsLiveData.value = Success(response.body()!!)
//                } else {
//                    _catsLiveData.value = Error(
//                        response.errorBody()?.string() ?: context.getString(
//                            R.string.default_error_text
//                        )
//                    )
//                }
//            }
//
//            override fun onFailure(call: Call<Fact>, t: Throwable) {
//                _catsLiveData.value = ServerError
//            }
//        })
        getFacts()
    }

    private fun getFacts() {
        val disposable = Observable.interval(DELAY_SECOND, TimeUnit.SECONDS)
            .flatMapSingle { catsService.getCatFactSingle() }
            .observeOn(Schedulers.io())
            .subscribeOn(AndroidSchedulers.mainThread())
            .onErrorResumeNext(::handleOnErrorResumeNext)
            .subscribe(::handleOnNext, ::handleOnError)

        compositeDisposable.add(disposable)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleOnErrorResumeNext(throwable: Throwable): Observable<Fact> {
        return localCatFactsGenerator.generateCatFactPeriodically().toObservable()
    }

    private fun handleOnNext(fact: Fact) {
        _catsLiveData.postValue(Success(fact))
    }

    private fun handleOnError(throwable: Throwable) {
        _catsLiveData.value = Error(
            message = throwable.message ?: context.getString(R.string.default_error_text)
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
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()