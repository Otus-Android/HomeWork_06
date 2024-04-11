package otus.homework.reactivecats

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

@SuppressLint("CheckResult")
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


//        val disposable = catsService.getCatFact()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({ fact ->
//                _catsLiveData.value = Success(fact)
//            }, { error ->
//                if (error is HttpException && error.code() == 500) {
//                    _catsLiveData.value = ServerError
//                } else {
//                    _catsLiveData.value =
//                        Error(error.message ?: context.getString(R.string.default_error_text))
//                }
//            })
//        compositeDisposable.add(disposable)


//        localCatFactsGenerator.generateCatFact().subscribe { fact ->
//            _catsLiveData.value = Success(fact)
//        }


//        localCatFactsGenerator.generateCatFactPeriodically()
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe { fact ->
//                _catsLiveData.value = Success(fact)
//            }
    }

    private fun getFacts() {
        val networkObservable = Observable.interval(0, 2, TimeUnit.SECONDS)
            .concatMapEager {
                catsService.getCatFact()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            }.onErrorResumeNext { exception: Throwable ->
                if (exception is HttpException && exception.code() == 500) {
                    _catsLiveData.value = ServerError
                } else {
                    _catsLiveData.value =
                        Error(exception.message ?: context.getString(R.string.default_error_text))
                }

                localCatFactsGenerator.generateCatFact()
            }

        val disposable = networkObservable.subscribe { fact ->
            _catsLiveData.value = Success(fact)
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
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
data object ServerError : Result()