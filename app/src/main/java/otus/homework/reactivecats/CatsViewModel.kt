package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit

class CatsViewModel(
    catsService: CatsService,
    localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val disposable = CompositeDisposable()

    init {
        getFacts(
            catsService = catsService,
            context = context,
            localCatFactsGenerator = localCatFactsGenerator
        )
    }

    private fun getFacts(
        catsService: CatsService,
        context: Context,
        localCatFactsGenerator: LocalCatFactsGenerator
    ) {
        disposable.add(
            Flowable.interval(0, 2000, TimeUnit.MILLISECONDS)
                .flatMapSingle {
                    catsService.getCatFact()
                        .onErrorResumeNext { localCatFactsGenerator.generateCatFact() }
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
                                val errorMessage = error.response()?.message()
                                    ?: context.getString(R.string.default_error_text)
                                Error(errorMessage)
                            }

                            is IOException -> ServerError
                            else ->
                                Error(
                                    error.message ?: context.getString(R.string.default_error_text)
                                )
                        }
                    }
                )
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }
}

@Suppress("UNCHECKED_CAST")
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
data object Loading : Result()
data object ServerError : Result()