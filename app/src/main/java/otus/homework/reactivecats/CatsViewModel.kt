package otus.homework.reactivecats

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    @SuppressLint("StaticFieldLeak") private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val compositeDisposable = CompositeDisposable()

    init {
        // Or call getFacts() instead
        compositeDisposable.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { fact -> _catsLiveData.value = Success(fact) },
                    { error ->
                        _catsLiveData.value = Error(
                            error.message ?: context.getString(R.string.default_error_text)
                        )
                    }
                )
        )
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.CatsViewModel#getFacts следующим образом:
     * каждые 2 секунды идем в сеть за новым фактом, если сетевой запрос завершился неуспешно,
     * то в качестве фоллбека идем за фактом в уже реализованный
     * otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact.
     */
    private fun getFacts() {
        compositeDisposable.add(
            Flowable.interval(2, TimeUnit.SECONDS)
                .flatMapSingle { catsService.getCatFact() }
                .onErrorResumeNext(localCatFactsGenerator.generateCatFact().toFlowable())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { fact -> _catsLiveData.value = Success(fact) },
                    { error ->
                        _catsLiveData.value = Error(
                            error.message ?: context.getString(R.string.default_error_text)
                        )
                    }
                )
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
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