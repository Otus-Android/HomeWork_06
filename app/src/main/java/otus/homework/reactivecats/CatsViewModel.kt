package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
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
    private var disposable: Disposable? = null
    val catsLiveData: LiveData<Result> = _catsLiveData

    private fun randomFact() =
        catsService.getCatFact()
            .onErrorResumeNext { localCatFactsGenerator.generateCatFact() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    private fun getFacts() =
        Flowable.interval(2000, 2000, TimeUnit.MILLISECONDS)
            .flatMap { randomFact().toFlowable() }
            .distinctUntilChanged()
            .subscribe({ fact ->
                _catsLiveData.value = Success(fact)
            }, { error ->
                _catsLiveData.value = Error(
                    error.message ?: context.getString(R.string.default_error_text)
                )
            })


    fun onStart() {
        disposable = getFacts()
    }

    fun onStopped() {
        disposable?.apply {
            dispose()
        }
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
data object ServerError : Result()