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

    private val compositeDispose = CompositeDisposable()

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
        val query = catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { fact -> _catsLiveData.value = Success(fact) },
                { error ->
                    _catsLiveData.value = Error(error.message ?: context.getString(R.string.default_error_text))
                }
            )
        compositeDispose.add(query)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDispose.clear()
    }

    fun getFacts() = catsService.getCatFact()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .onErrorResumeNext(localCatFactsGenerator.generateCatFact())
        .repeatWhen { it.delay(QUERY_DELAY_IN_SECOND, TimeUnit.SECONDS) }
        .subscribe({
            _catsLiveData.value = Success(it!!)
        }, {
            _catsLiveData.value = Error(
                it.message ?: context.getString(R.string.default_error_text)
            )
        })

    companion object {
        private const val QUERY_DELAY_IN_SECOND: Long = 2
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