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
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val disposables: CompositeDisposable = CompositeDisposable()

    init {
        val localDisposable = catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { fact -> _catsLiveData.value = Success(fact) },
                { error -> handleError(error) }
            )
        disposables.add(localDisposable)

    }

    override fun onCleared() {
        disposables.dispose()
    }

    private  fun handleError(error: Throwable){
        Log.e(REACTIVE_CATS_ERROR, "error $error")
        _catsLiveData.value = Error(error.message?: context.getString(
            R.string.default_error_text
        ))
    }

    fun getFacts() {
        val localDisposable = catsService.getCatFact()
            .onErrorResumeNext(localCatFactsGenerator.generateCatFact())
            .repeatWhen { it.delay(2, TimeUnit.SECONDS) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { success -> _catsLiveData.value = Success(success) },
                { error -> handleError(error) }
            )

        disposables.add(localDisposable)
    }

    private companion object{
        const val REACTIVE_CATS_ERROR = "reactiveCatsError"
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