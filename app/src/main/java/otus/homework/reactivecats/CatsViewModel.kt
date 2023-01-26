package otus.homework.reactivecats

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val disposables = CompositeDisposable()

    init {
        getFacts()
    }

    private fun getFacts() {
        val disposable = catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .onErrorResumeNext(localCatFactsGenerator.generateCatFact())
            .delay(2000L, TimeUnit.MILLISECONDS)
            .repeat()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { fact ->
                    _catsLiveData.value = Success(fact)
                },
                { throwable ->
                    if (throwable is IOException) {
                        Log.e("CatsViewModel", "Что-то не так с интернетом", throwable)
                        _catsLiveData.value = ServerError
                    } else {
                        Log.e("CatsViewModel", "Что-то пошло не так", throwable)
                        _catsLiveData.value = Error(
                            throwable.message ?: context.getString(
                                R.string.default_error_text
                            )
                        )
                    }
                }
            )
        disposables.add(disposable)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
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