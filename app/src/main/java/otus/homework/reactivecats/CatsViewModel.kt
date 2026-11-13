package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private var disposables = CompositeDisposable()

    init {
        observeFacts(
            getFacts().toObservable()
        )
    }

    private fun observeFacts(
        observable: Observable<Fact>
    ) {
        val disposable =
            observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _catsLiveData.value = Success(it)
                }, { th ->
                    _catsLiveData.value = if (th is HttpException) {
                        Error(
                            th.response()?.errorBody()?.string() ?: context.getString(
                                R.string.default_error_text
                            )
                        )
                    } else {
                        ServerError
                    }
                })

        disposables.add(disposable)
    }

    fun getFacts(): Flowable<Fact> {
        return Flowable.interval(2, TimeUnit.SECONDS)
            .switchMapSingle {
                getFactFromNetwork()
                    .onErrorResumeNext {
                        localCatFactsGenerator.generateCatFact()
                    }
            }
            .distinctUntilChanged()
    }

    private fun getFactFromNetwork(): Single<Fact> {
        return catsService
            .getCatFact()
            .subscribeOn(Schedulers.io())
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
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