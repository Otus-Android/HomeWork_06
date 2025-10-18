package otus.homework.reactivecats

import android.content.Context
import android.net.http.HttpException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okio.IOException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val сompositeDisposable = CompositeDisposable()

    init {
        getFacts()
    }

    fun getFacts() {

        val disposable = Observable.interval(0, 2000, TimeUnit.MILLISECONDS)
            .flatMapSingle {
                catsService.getCatFact().onErrorResumeNext { localCatFactsGenerator.generateCatFact() }
            }
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _catsLiveData.value = Success(it)
            }, {
                when (it) {
                    is IOException -> {
                        _catsLiveData.value = ServerError
                    }
                    else -> {
                        _catsLiveData.value = Error(it.message.toString())
                    }
                }
            })
        сompositeDisposable.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        сompositeDisposable.dispose()
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator
) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()