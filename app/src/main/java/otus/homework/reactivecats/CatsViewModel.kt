package otus.homework.reactivecats

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit

@SuppressLint("CheckResult")
class CatsViewModel(
    catsService: CatsService,
    localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    // Для управления подписками и их очистки
    private val disposables = CompositeDisposable()

    init {
        disposables.add(
            Flowable
                .interval(2, TimeUnit.SECONDS)
                .flatMapSingle {
                    catsService.getCatFact()
                        .onErrorResumeNext { error ->
                            if (error is IOException || error is HttpException) {
                                localCatFactsGenerator.generateCatFact()
                            } else {
                                Single.error(error)
                            }
                        }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { fact ->
                        _catsLiveData.value = Success(fact)
                    },
                    { error ->
                        _catsLiveData.value = when (error) {
                            is HttpException -> {
                                val errorMessage = error.response()?.errorBody()?.string()
                                    ?: context.getString(R.string.default_error_text)
                                Error(errorMessage)
                            }
                            is IOException -> ServerError
                            else -> Error(error.message ?: context.getString(R.string.default_error_text))
                        }
                    }
                )
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
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