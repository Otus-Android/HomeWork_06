package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.io.IOException

class CatsViewModel(
    catsService: CatsService,
    localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val compositeDisposable = CompositeDisposable()

    init {
        catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .map<Result>{ fact -> Success(fact) }
            .subscribe(object : SingleObserver<Result> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {
                    val result = when (e) {
                        is HttpException -> {
                            Error(e.message())
                        }
                        is IOException -> {
                            ServerError
                        }
                        else -> {
                            Error(e.message ?: context.getString(R.string.default_error_text))
                        }
                    }
                    _catsLiveData.postValue(result)
                }

                override fun onSuccess(t: Result) {
                   _catsLiveData.postValue(t)
                }
            })
    }

    fun getFacts() {}

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

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()