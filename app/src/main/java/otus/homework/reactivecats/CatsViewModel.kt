package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers


class CatsViewModel(
    catsService: CatsService,
//    localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private var _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    var mySubsribe: Disposable

    init {
        val retro = DiContainer().service.getCatFact()
        mySubsribe = retro.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ _catsLiveData.value = Result.Success(it) },
                { _catsLiveData.value = Result.Error(it.message.toString()) }
            )
    }

    override fun onCleared() {
        super.onCleared()
        mySubsribe.dispose()
    }

    fun getFacts() {}
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
//    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        CatsViewModel(
            catsRepository,
//            localCatFactsGenerator,
            context
        ) as T
}

sealed class Result {
    data class Success(val fact: Fact) : Result()
    data class Error(val message: String) : Result()
    object ServerError : Result()
}