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
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private var disposable: Disposable? = null

    init {
       getFacts()
    }

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
        disposable = null
    }

    private fun getFacts() {
        disposable = catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .onErrorResumeNext {
                localCatFactsGenerator.generateCatFact()
            }
            .repeatWhen {
                it.delay(2000, TimeUnit.MILLISECONDS)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    _catsLiveData.postValue(Success(it))
                },
                {
                    _catsLiveData.postValue(
                        Error(
                            it.message ?: context.getString(R.string.default_error_text)
                        )
                    )
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