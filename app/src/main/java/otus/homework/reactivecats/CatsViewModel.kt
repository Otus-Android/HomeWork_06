package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import java.util.concurrent.TimeUnit.SECONDS

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    val disposable = CompositeDisposable()

    init {
        disposable.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { response ->
                        if (response.isSuccessful && response.body() != null) {
                            _catsLiveData.value = Success(response.body()!!)
                        } else {
                            _catsLiveData.value = Error(
                                response.errorBody()?.string() ?: context.getString(
                                    R.string.default_error_text
                                )
                            )
                        }
                    },
                    {
                        _catsLiveData.value = ServerError
                    }
                )
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }

    fun getFacts() {
        disposable.add(
            Observable.interval(0, 2, SECONDS)
                .flatMap { catsService.getCatFact().toObservable() }
                .map { response: Response<Fact> ->
                    if (response.isSuccessful && response.body() != null) {
                        response.body()!!
                    } else {
                        throw Throwable("Не удалось загрузить")
                    }
                }
                .onErrorResumeNext(
                    localCatFactsGenerator.generateCatFactPeriodically().toObservable()
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { fact ->
                    _catsLiveData.value = Success(fact)
                }
        )
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