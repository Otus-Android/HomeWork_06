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
    catsService: CatsService,
    localCatFactsGenerator: LocalCatFactsGenerator,
) : ViewModel() {

    private val disposables = CompositeDisposable()
    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
        disposables.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { response ->
                        if (response.isSuccessful && response.body() != null) {
                            _catsLiveData.value = Success(response.body()!!)
                        } else {
                            _catsLiveData.value = response.errorBody()?.string()?.let {
                                Error.Message(it)
                            } ?: Error.ResId(R.string.default_error_text)
                        }

                    },
                    { _catsLiveData.value = ServerError }
                )
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

    fun getFacts() {}
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