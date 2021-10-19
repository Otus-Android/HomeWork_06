package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val disposables = CompositeDisposable()

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
        getFacts()
    }

    fun getFacts() {
        Flowable
            .interval(2000, TimeUnit.MILLISECONDS)
            .onBackpressureDrop()
            .flatMapSingle {
                catsService.getCatFact()
                    .onErrorResumeNext { localCatFactsGenerator.generateCatFact() }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { r ->
                    _catsLiveData.value = Success(r)
                },
                { th ->
                    _catsLiveData.value =
                        Error(th.message ?: context.getString(R.string.default_error_text))
                }
            )
            .addTo(disposables)
    }

    fun getFact() {
        catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { r -> _catsLiveData.value = Success(r) },
                { th ->
                    _catsLiveData.value =
                        Error(th.message ?: context.getString(R.string.default_error_text))
                }
            )
            .addTo(disposables)
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
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()