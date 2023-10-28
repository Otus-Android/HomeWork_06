package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private lateinit var disposable: Disposable

    init {
        getFacts()
    }

    private fun getFacts() {
        disposable = Flowable.interval(DELAY, TimeUnit.MILLISECONDS)
            .flatMapSingle { catsService.getCatFact() }
            .onErrorResumeNext { _: Throwable -> localCatFactsGenerator.generateCatFact().toFlowable() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { fact: Fact -> _catsLiveData.value = Success(fact) },
                { throwable: Throwable ->
                    _catsLiveData.value = Error(
                        throwable.message ?: context.getString(
                            R.string.default_error_text
                        )
                    )
                })
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }

   private companion object {
        const val DELAY = 2L
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