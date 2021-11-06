package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val disposable = CompositeDisposable()

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
        disposable.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                    object : DisposableSingleObserver<Fact>() {

                        override fun onSuccess(t: Fact) {
                            _catsLiveData.value = Success(t)
                        }

                        override fun onError(e: Throwable) {
                            _catsLiveData.value = Error(
                                e.message ?: context.getString(
                                    R.string.default_error_text
                                )
                            )
                        }
                    }
                )
        )
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.CatsViewModel#getFacts следующим образом:  каждые 2 секунды идем в сеть
     * за новым фактом, если сетевой запрос завершился неуспешно, то в качестве фоллбека идем за фактом в уже реализованный
     * otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact.

     */
    private fun getFacts() =
        disposable.add(
            Observable.interval(2000L, TimeUnit.MILLISECONDS)
                .flatMapSingle { catsService.getCatFact() }
                .doOnError { localCatFactsGenerator.generateCatFact() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                    object : DisposableObserver<Fact>() {

                        override fun onError(e: Throwable) {
                            _catsLiveData.value = Error(
                                e.message ?: context.getString(
                                    R.string.default_error_text
                                )
                            )
                        }

                        override fun onNext(t: Fact) {
                            _catsLiveData.value = Success(t)
                        }

                        override fun onComplete() {}
                    }
                )
        )


    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
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