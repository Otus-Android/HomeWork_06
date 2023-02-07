package otus.homework.reactivecats

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

@SuppressLint("CheckResult")
class CatsViewModel(
    catsService: CatsService,
    localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private var disposable = CompositeDisposable()

    companion object {
        private const val INITIAL_DELAY = 0L
        private const val PERIOD = 2000L
    }

    init {
        val factDisposable =
            Observable.interval(INITIAL_DELAY, PERIOD, TimeUnit.MILLISECONDS)
                .flatMapSingle { catsService.getCatFact() }
                .subscribeOn(Schedulers.io())
                .onErrorResumeNext(localCatFactsGenerator.generateCatFact().toObservable())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _catsLiveData.value = Success(it)
                }, {
                    _catsLiveData.value = Error(
                        it.message ?: context.getString(
                            R.string.default_error_text
                        )
                    )
                })

        disposable.add(factDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()