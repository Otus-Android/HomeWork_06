package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class CatsViewModel(
    catsService: CatsService,
    localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
        compositeDisposable.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    onSuccess = { fact ->
                        _catsLiveData.value = Success(fact)
                    },
                    onError = { error ->
                        _catsLiveData.value = Error(
                            error ?: context.getString(R.string.default_error_text)
                        )
                    },
                    onFailure = {
                        _catsLiveData.value = ServerError
                    }
                )
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    fun getFacts() {}

    private fun <T, R : Response<T>> Single<R>.subscribe(
        onSuccess: (T) -> Unit,
        onError: (String?) -> Unit,
        onFailure: (Throwable) -> Unit
    ): Disposable {
        return subscribe(
            { response ->
                val responseBody = response.body()

                if (response.isSuccessful && responseBody != null) {
                    onSuccess(responseBody)
                } else {
                    onError(response.errorBody()?.string())
                }
            },
            { throwable ->
                onFailure(throwable)
            }
        )
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
