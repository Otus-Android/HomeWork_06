package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val errorMsg = context.getString(R.string.default_error_text)
    private val compositeDisposable = CompositeDisposable()
    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
        getFacts()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    private fun getFacts() {
        catsService.getCatFact()
            .onErrorResumeNext { localCatFactsGenerator.generateCatFact() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                ::handleSuccess,
                ::handleError
            ).let(::addToCompositeDisposable)
    }

    private fun getFactPeriodically() {
        localCatFactsGenerator.generateCatFactPeriodically()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                ::handleSuccess,
                ::handleError
            ).let(::addToCompositeDisposable)
    }


    private fun handleSuccess(fact: Fact) {
        _catsLiveData.value = Success(fact)
    }

    private fun handleError(error: Throwable) {
        _catsLiveData.value = if (error is HttpException) {
            ServerError
        } else {
            Error(error.message ?: errorMsg)
        }
    }

    private fun addToCompositeDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
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
data object ServerError : Result()