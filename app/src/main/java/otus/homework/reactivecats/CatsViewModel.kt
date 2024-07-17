package otus.homework.reactivecats

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.concurrent.TimeUnit

class CatsViewModel(
    catsService: CatsService,
    localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsService = catsService
    private val _localCatFactsGenerator = localCatFactsGenerator

    val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val compositeDisposable = CompositeDisposable()

    init {
        compositeDisposable.add(
            _catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(localCatFactsGenerator.generateCatFact())
                .subscribe(
                    { i -> setValue(Success(i)) },
                    { e -> if ( e is IOException )
                                setValue(ServerError)
                            else
                                setValue(Error(e.message))
                    }
                )
        )
        getFacts()
    }

    private fun getFacts() {
/*
        compositeDisposable.add(
            Flowable
                .interval(2, TimeUnit.SECONDS)
                .flatMapSingle {
                    _catsService.getCatFact()
                        .onErrorResumeNext(_localCatFactsGenerator.generateCatFact())
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { i -> setValue(Success(i)) },
                    { e -> if ( e is IOException )
                        setValue(ServerError)
                    else
                        setValue(Error(e.message))
                    }
                )
        )
*/

        compositeDisposable.add(
            _catsService.getCatFact()
                .onErrorResumeNext(_localCatFactsGenerator.generateCatFact())
                .delay(2000, TimeUnit.MILLISECONDS)
                .repeat()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { i -> setValue(Success(i)) },
                    { e -> if ( e is IOException )
                                setValue(ServerError)
                            else
                                setValue(Error(e.message))
                    }
                )

        )
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("${javaClass.name}#onCleared()", "")
        compositeDisposable.dispose()
    }

    private fun setValue(r:Result) {
        Log.d("${javaClass.name}#setValue()", "$r")
        _catsLiveData.value = r
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
data class Error(val message: String?) : Result()
object ServerError : Result()