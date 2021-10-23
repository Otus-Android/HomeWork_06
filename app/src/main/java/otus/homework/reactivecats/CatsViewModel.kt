package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

class CatsViewModel(
    catsService: CatsService,
    localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {
    
    private val disposables = CompositeDisposable()
    
    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
        val factDisposable = catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { fact ->
                    _catsLiveData.value = Success(fact)
                },
                { e ->
                    _catsLiveData.value = handleError(e, context)
                }
            )
        disposables.add(factDisposable)
    }
    
    private fun handleError(e: Throwable, context: Context): Result {
        return if (e is HttpException) {
            Error(e.response()?.errorBody()?.string() ?: context.getString(R.string.default_error_text))
        } else {
            ServerError
        }
    }

    fun getFacts() {}
    
    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
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