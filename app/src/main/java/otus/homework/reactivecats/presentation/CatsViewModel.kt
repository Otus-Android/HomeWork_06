package otus.homework.reactivecats.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import otus.homework.reactivecats.CatsService
import otus.homework.reactivecats.LocalCatFactsGenerator
import otus.homework.reactivecats.utils.Result
import otus.homework.reactivecats.utils.Result.Error
import otus.homework.reactivecats.utils.Result.Success
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit.SECONDS

class CatsViewModel(
    catsService: CatsService,
    localCatFactsGenerator: LocalCatFactsGenerator
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private var disposable: Disposable? = null

    init {
        getFacts(catsService = catsService)
    }

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }
    private fun getFacts(catsService: CatsService) {
        disposable = Flowable
            .interval(2,SECONDS)
            .flatMapSingle { catsService.getCatFact() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { _catsLiveData.value =  Success(fact = it) }
            .doOnError { _catsLiveData.value = Error(message = it.toString()) }
            .subscribe()
    }
}
