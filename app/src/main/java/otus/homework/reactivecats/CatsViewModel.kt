package otus.homework.reactivecats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val catsService: CatsService,
    private val printableText: PrintableText
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val compositeDisposable = CompositeDisposable()

    init {
        catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleResponse) {
                it.printStackTrace()
                _catsLiveData.value = ServerError
            }
            .addTo(compositeDisposable)
    }

    fun getFacts() {
        Observable.interval(2, TimeUnit.SECONDS)
            .flatMap { getCatFact() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleResponse)
            .addTo(compositeDisposable)
    }

    private fun getCatFact() = catsService
        .getCatFact()
        .toObservable()
        .observeOn(Schedulers.io())
        .onErrorResumeNext(localCatFactsGenerator.generateCatFact().map { Response.success(it) })

    private fun handleResponse(response: Response<Fact>) {
        _catsLiveData.value = if (response.isSuccessful && response.body() != null) {
            Success(response.body()!!)
        } else {
            Error(printableText.getErrorOrDefault(response.errorBody()?.string()))
        }
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val printableText: PrintableText
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        CatsViewModel(localCatFactsGenerator, catsRepository, printableText) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()

fun Disposable.addTo(compositeDisposable: CompositeDisposable): Disposable {
    compositeDisposable.add(this)
    return this
}