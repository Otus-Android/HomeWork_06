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

class CatsViewModel(
    catsService: CatsService,
    localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val tag = javaClass.simpleName
    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val subscriptionDisposables: CompositeDisposable = CompositeDisposable()

    init {

        val catFactsSubscription = catsService
            .getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { fact ->
                    _catsLiveData.value = Success(fact)
                },
                { error ->
                    if (error is retrofit2.HttpException) {
                        Log.e(tag, "ServerError: ${error.code()}")
                        _catsLiveData.value = ServerError
                    } else {
                        Log.e(tag, "Error: $error")
                        _catsLiveData.value = Error(
                            error.message ?: context.getString(R.string.default_error_text)
                        )
                    }
                }
            )

        subscriptionDisposables.add(catFactsSubscription)
    }

    fun getFacts() {}

    override fun onCleared() {
        super.onCleared()
        subscriptionDisposables.dispose()
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()