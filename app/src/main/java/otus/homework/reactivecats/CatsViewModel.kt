package otus.homework.reactivecats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private var periodicalDisposable: Disposable? = null
    private var localPeriodicalDisposable: Disposable? = null
    private var localSingleDisposable: Disposable? = null

    init {
        // получение фактов от сервера и локальных в случае ошибки
        getFactsPeriodically()
        // получение локальных файлов
        // viewModelScope.launch(Dispatchers.IO) { observeLocalFacts() }
    }

    private fun getFacts(onError: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            catsService.getCatFact().subscribe(
                { response ->
                    response.record?.let { fact ->
                        onFactReceived(
                            fact.copy(text = fact.text + System.currentTimeMillis().toString())
                        )
                    }
                },
                { error ->
                    onError.invoke()
                    onErrorReceived(error)
                }
            )
        }
    }

    private fun getFactsPeriodically() {
        periodicalDisposable = Observable.interval(2, TimeUnit.SECONDS)
            .observeOn(Schedulers.io()).subscribe {
                getFacts(onError = { getLocalFact() })
            }
    }

    private fun getLocalFact() {
        localSingleDisposable = localCatFactsGenerator.generateCatFact().subscribe { fact ->
            onFactReceived(fact)
        }
    }

    private fun observeLocalFacts() {
        localPeriodicalDisposable =
            localCatFactsGenerator.generateCatFactPeriodically().subscribe { fact ->
                onFactReceived(fact)
            }
    }

    private fun onFactReceived(fact: Fact) {
        viewModelScope.launch {
            _catsLiveData.value = Success(fact)
        }
    }

    private fun onErrorReceived(error: Throwable) {
        viewModelScope.launch {
            _catsLiveData.value = Error(error.toString())
        }
    }

    override fun onCleared() {
        localSingleDisposable?.dispose()
        localPeriodicalDisposable?.dispose()
        periodicalDisposable?.dispose()
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()