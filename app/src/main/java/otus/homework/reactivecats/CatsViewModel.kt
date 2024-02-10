package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private var compositeDisposable = CompositeDisposable()

    init {
        getFacts()
    }

    private fun getFacts() {
        compositeDisposable.add(
            Flowable.interval(0, 2, TimeUnit.SECONDS).flatMap {
                getCatFactUseCase()
                    .onErrorResumeNext {
                        getLocalCatFactUseCase()
                    }.toFlowable()
                    .subscribeOn(Schedulers.io())

            }.observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _catsLiveData.value = Success(it)
                }, {
                    _catsLiveData.value = Error(
                        it.message ?: context.getString(
                            R.string.default_error_text
                        )
                    )
                })
        )
    }

    private var remoteCatFactCount = 0
    private var localCatFactCount = 0

    private fun getCatFactUseCase(): Single<Fact> {
        return Single.just(Unit).flatMap {
            remoteCatFactCount++
            if (remoteCatFactCount % 4 == 0) {
                throw Exception("getCatFactUseCase exception")
            } else {
                catsService.getCatFact()
            }
        }
    }

    private fun getLocalCatFactUseCase(): Single<Fact> {
        return Single.just(Unit).flatMap {
            localCatFactCount++
            if (localCatFactCount % 2 == 0) {
                throw Exception("getLocalCatFactUseCase exception")
            } else {
                localCatFactsGenerator.generateCatFact()
            }
        }
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

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()