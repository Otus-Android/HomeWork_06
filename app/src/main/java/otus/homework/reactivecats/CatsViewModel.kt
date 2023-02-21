package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit.MILLISECONDS


class CatsViewModel(
    catsService: CatsService,
    context: Context
) : ViewModel() {

    companion object {
        private const val TIME_REQUEST = 2000L       //ms
        private const val WAIT_RESPONSE = 240L       //ms
    }

    private var _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    var myDispose: Disposable
    val locFlow = LocalCatFactsGenerator(context)
    val timeOut = TIME_REQUEST + WAIT_RESPONSE

    init {
        myDispose = getFacts(catsService)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ _catsLiveData.value = Result.Success(it) },
                { _catsLiveData.value = Result.Error(it.message.toString()) }
            )
    }

    override fun onCleared() {
        super.onCleared()
        myDispose.dispose()

    }

    fun getFacts(catsService: CatsService): Flowable<Fact> {

        return catsService.getCatFact()
            .delay(TIME_REQUEST, MILLISECONDS)
            .timeout(timeOut, MILLISECONDS, locFlow.generateCatFact())
            .repeat()
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
        CatsViewModel(DiContainer().service, context ) as T
}

sealed class Result {
    data class Success(val fact: Fact) : Result()
    data class Error(val message: String) : Result()
    object ServerError : Result()
}