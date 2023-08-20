package otus.homework.reactivecats

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val disposables = CompositeDisposable()

    private fun handleSuccess(fact: Fact) {
        _catsLiveData.postValue(Success(fact))
        disposables.clear()
    }

    private fun handleNetError(t: Throwable) {
        disposables.add(localCatFactsGenerator
            .generateCatFact()
            .subscribe (
                this::handleSuccess,
                this::handleError
            )
        )
    }

    private fun handleError(t: Throwable) {
        _catsLiveData.postValue(Error(t.message.toString()))
        disposables.clear()
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    init {
        viewModelScope.launch {
            while (true) {
                delay(2000)
                getFacts()
            }
        }
    }

    fun getFacts() {
        disposables.add(
            catsService.getCatFact()
                //.observeOn(AndroidSchedulers.mainThread()) // postValue is thread-safe
                .subscribeOn(Schedulers.io())
                .subscribe(
                    this::handleSuccess,
                    this::handleNetError)
        )
        Log.d("***[", "disposables.size=${disposables.size()}")

    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        if (modelClass.isAssignableFrom(CatsViewModel::class.java)) {
            CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
        } else {
            throw IllegalArgumentException("Expected CatsViewModel, but ${modelClass::class.java.name}")
        }
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()
