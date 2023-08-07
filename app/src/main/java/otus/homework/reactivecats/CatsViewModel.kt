package otus.homework.reactivecats

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

@SuppressLint("SuspiciousIndentation")
class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val compositeDisposable = CompositeDisposable()
    private val interval: Long = 2000

    init {
        compositeDisposable.add(
            Observable
                .interval(interval, TimeUnit.MILLISECONDS)
                .repeat()
                .subscribe {
                    getFacts()
                }
        )

    }

    @SuppressLint("CheckResult")
    private fun getFacts() {
        Flowable
            .timer(0, TimeUnit.MILLISECONDS)
            .flatMap {
                catsService.getCatFact().toFlowable()
                    .getResult(ErrorTypes.SERVER_ERROR)
            }

            .onErrorResumeNext { _: Throwable ->
                localCatFactsGenerator
                    .generateCatFact()
                    .toFlowable()
                    .getResult(ErrorTypes.ERROR_RESULT)
            }

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { _catsLiveData.value = it }


    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
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





