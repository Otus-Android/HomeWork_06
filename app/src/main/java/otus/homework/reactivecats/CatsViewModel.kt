package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.io.IOException

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

    fun getFacts() {
        val disposable = catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { fact ->
                    _catsLiveData.value = Success(fact)
                },
                { throwable ->
                    handleError(throwable)
                }
            )

        compositeDisposable.add(disposable)
    }

    private fun handleError(throwable: Throwable) {
        when (throwable) {
            is HttpException -> {
                val errorBody = throwable.response()?.errorBody()?.string()
                    ?: context.getString(R.string.default_error_text)
                _catsLiveData.value = Error(errorBody)
            }
            is IOException -> {
                _catsLiveData.value = ServerError
            }
            else -> {
                _catsLiveData.value = Error(context.getString(R.string.default_error_text))
            }
        }
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

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()