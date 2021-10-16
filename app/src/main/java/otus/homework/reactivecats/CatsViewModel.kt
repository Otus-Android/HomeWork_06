package otus.homework.reactivecats

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
    private val compositeDisposable: CompositeDisposable = CompositeDisposable(),
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
        catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { fact ->
                    setFact(fact, context)
                },
                {
                    _catsLiveData.value = ServerError
                }
            ).addTo(compositeDisposable)
    }

    private fun setFact(
        fact: Fact?,
        context: Context
    ) {
        if (fact != null) {
            _catsLiveData.value = Success(fact)
        } else {
            _catsLiveData.value = Error(
                context.getString(R.string.default_error_text)
            )
        }
    }

    fun getFacts() {
        Flowable.interval(2000L, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .flatMap {
                catsService.getCatFact().toFlowable()
                    .onErrorReturnItem(
                        localCatFactsGenerator.generateCatFact().blockingGet()
                    )
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { fact ->
                    if (fact != null) {
                        _catsLiveData.value = Success(fact)
                    }
                },
                { }
            ).addTo(compositeDisposable)
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
        CatsViewModel(
            catsService = catsRepository,
            localCatFactsGenerator = localCatFactsGenerator,
            context = context
        ) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()

fun Disposable.addTo(compositeDisposable: CompositeDisposable): Disposable =
    apply { compositeDisposable.add(this) }