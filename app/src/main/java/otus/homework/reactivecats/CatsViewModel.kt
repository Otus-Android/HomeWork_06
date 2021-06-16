package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

const val PERIOD_EMIT = 2000L

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
        //task 1,2
        getFactWithErrorMessage(context)

        //task 3-5
//        getFacts()
    }

    private fun getFactWithErrorMessage(context: Context) {
        compositeDisposable.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ fact ->
                    if (fact != null) {
                        _catsLiveData.value = Success(fact)
                    } else {
                        _catsLiveData.value =
                            Error(context.getString(R.string.default_error_text))
                    }
                }, {
                    _catsLiveData.value = ServerError
                })
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    private fun getFacts() {
        val remoteFacts = catsService.getCatFact()

        val localFact = localCatFactsGenerator.generateCatFact()
            .toFlowable()
            .onBackpressureDrop()

        compositeDisposable.add(remoteFacts
            .delay(PERIOD_EMIT, TimeUnit.MILLISECONDS)
            .onErrorResumeNext(localFact)
            .repeat()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { fact ->
                _catsLiveData.value = Success(fact)
            })

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