package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private var disposable: Disposable? = null

    init {
        disposable = catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ success ->
                if (success != null) {
                    _catsLiveData.value = Success(success)
                }
            }, { error ->
                _catsLiveData.value = Error(
                    error.message ?: context.getString(
                        R.string.default_error_text
                    )
                )
            })
    }

    override fun onCleared() {
        super.onCleared()
        disposable = null
    }

    fun getFacts(context: Context) {
        disposable = catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorResumeNext { localCatFactsGenerator.generateCatFact() }
            .repeatWhen { it.delay(2, TimeUnit.SECONDS) }
            .subscribe({ success ->
                if (success != null) {
                    _catsLiveData.value = Success(success)
                }
            }, { error ->
                _catsLiveData.value = Error(
                    error.message ?: context.getString(
                        R.string.default_error_text
                    )
                )
            })

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