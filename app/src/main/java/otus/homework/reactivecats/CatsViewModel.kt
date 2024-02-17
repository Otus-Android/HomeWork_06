package otus.homework.reactivecats

import android.content.Context
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
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val disposables = CompositeDisposable()

    init {
        getFacts()
    }

    fun getFacts() {
        disposables.add(
//            localCatFactsGenerator.generateCatFact()
            localCatFactsGenerator.generateCatFactPeriodically()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { fact ->
                    _catsLiveData.value = Success(fact)
                }
//            catsService.getCatFact()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(
//                    { fact -> _catsLiveData.value = Success(fact)},
//                    { error -> handleFailure(error) }
//                )
        )
    }

    private fun handleFailure(e: Throwable) {
        if (e is retrofit2.HttpException) {
            // ошибка HTTP, можно получить errorBody
            _catsLiveData.value = Error(
                e.response()?.errorBody()?.string() ?: context.getString(R.string.default_error_text)
            )
        } else {
            // не http ошибка
            _catsLiveData.value = ServerError
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
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