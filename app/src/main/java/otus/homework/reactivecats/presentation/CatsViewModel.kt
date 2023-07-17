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

class CatsViewModel(
    catsService: CatsService,
    localCatFactsGenerator: LocalCatFactsGenerator
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
        getFacts(catsService = catsService)
    }

    private fun getFacts(catsService: CatsService) {

        catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { _catsLiveData.value =  Success(fact = it) }
            .doOnError { _catsLiveData.value = Error(message = it.toString()) }
            .doOnDispose { this.onCleared() }
            .subscribe()
    }
}
