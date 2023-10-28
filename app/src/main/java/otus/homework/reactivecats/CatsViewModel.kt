package otus.homework.reactivecats

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
) : ViewModel() {

    private val disposables = CompositeDisposable()
    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
        getFacts()
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

    fun getFacts() {
        val disposable = catsService.getCatFact()
            .onErrorResumeNext(localCatFactsGenerator.generateCatFact())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .repeat(DELAY_BETWEEN_FETCH_FACT)
            .subscribe(
                { _catsLiveData.value = Success(it) },
                { _catsLiveData.value = ServerError }
            )
        disposables.add(disposable)
    }

    companion object {
        private const val DELAY_BETWEEN_FETCH_FACT = 2000L
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
sealed class Error : Result() {
    data class Message(val message: String) : Error()
    data class ResId(@StringRes val resId: Int) : Error()
}

object ServerError : Result()