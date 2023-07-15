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
    @Suppress("StaticFieldLeak") private val context: Context
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
        getFacts()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    fun getFacts() {
        // .onErrorResumeNext(localCatFactsGenerator.generateCatFactPeriodically()) можно заменить на
        // .onErrorResumeNext(localCatFactsGenerator.generateCatFact().toFlowable()) как по заданию.
        // Но использование generateCatFactPeriodically делает поведение в случае ошибки похожим на поход в сеть
        compositeDisposable.add(
            Flowable
                .interval(2, TimeUnit.SECONDS)
                .flatMapSingle { catsService.getCatFact() }
                .onErrorResumeNext(localCatFactsGenerator.generateCatFactPeriodically())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { fact ->
                        _catsLiveData.value = Success(fact)
                    },
                    { throwable ->
                        _catsLiveData.value =
                            Error(
                                throwable.message ?: context.getString(R.string.default_error_text)
                            )
                    }
                )
        )
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
