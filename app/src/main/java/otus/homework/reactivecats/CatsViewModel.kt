package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private var subscriptions: CompositeDisposable = CompositeDisposable()

    init {
        subscriptions.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { _catsLiveData.value = Success(it) },
                    {
                        val message = it.message ?: context.getString(R.string.default_error_text)
                        _catsLiveData.value = Error(message)
                    }
            )
        )
        getFacts()
    }

    fun getFacts() {
        subscriptions.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .delay(2000L, TimeUnit.MILLISECONDS)
                .repeat()
                .onExceptionResumeNext {
                    localCatFactsGenerator.generateCatFact()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _catsLiveData.value = Success(it)
                    },
                    {
                        val message = it.message ?: context.getString(R.string.default_error_text)
                        _catsLiveData.value = Error(message)
                    }
                )
        )
    }

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
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