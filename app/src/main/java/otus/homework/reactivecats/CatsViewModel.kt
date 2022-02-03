package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.disposables.CompositeDisposable

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val compositeDisposable = CompositeDisposable()

    init {
        catsService.getCatFact()
            .observeFromIoToMain()
            .subscribe({ fact ->
                _catsLiveData.value = Success(fact)
            },{ throwable ->
                _catsLiveData.value = Error(throwable.message
                    ?: context.getString(R.string.default_error_text))
            }).addTo(compositeDisposable)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
    }

    fun getFacts() {
        localCatFactsGenerator.generateCatFactPeriodically()
            .observeFromIoToMain()
            .onErrorResumeNext { _: Throwable ->
                localCatFactsGenerator.generateCatFact().toFlowable()
            }
            .subscribe({ fact ->
                _catsLiveData.value = Success(fact)
            }, { throwable ->
                _catsLiveData.value = Error(
                    throwable.message
                        ?: context.getString(R.string.default_error_text)
                )
            }).addTo(compositeDisposable)
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