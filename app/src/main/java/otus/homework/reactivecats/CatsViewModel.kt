package otus.homework.reactivecats

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "facts"
    }

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val disposables = CompositeDisposable()

    private val resources = context.resources

    init {
        getFact()
    }

    private fun getFact() {
        catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { _catsLiveData.value = Success(it) },
                { error ->
                    _catsLiveData.value = Error(
                        error.message ?: resources.getString(R.string.default_error_text)
                    )
                }
            ).also { disposables.add(it) }
    }

    private fun getFactsPeriodicallyObservable(): Observable<Fact> =
        Observable.interval(2000, TimeUnit.MILLISECONDS)
            .flatMapSingle {
                Log.i(TAG, "attempting to get fact")
                catsService.getCatFact()
            }

    fun getFacts() {

        getFactsPeriodicallyObservable()
            .onErrorReturn { localCatFactsGenerator.generateCatFact() }
            .repeat()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Log.i(TAG, "fact: ${it.text}")
                _catsLiveData.value = Success(it)
            }.also {
                disposables.add(it)
            }
    }

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

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()