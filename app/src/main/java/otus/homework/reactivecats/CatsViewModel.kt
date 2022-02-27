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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    val disposable: CompositeDisposable = CompositeDisposable()
    val _context = context

    init {
        catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .subscribeOn(AndroidSchedulers.mainThread())
            .doOnError {
                _catsLiveData.value = ServerError
            }
            .subscribe {response ->
                if (response.isSuccessful && response.body() != null) {
                    _catsLiveData.value = Success(response.body()!!)
                } else {
                    response.errorBody()?.string() ?: context.getString(R.string.default_error_text)
                }
            }
            .apply {
                disposable.add(this)
            }
    }

    fun getFacts() : Observable<Fact> {
        return Observable.interval(2, TimeUnit.SECONDS)
            .flatMap {
                catsService.getCatFact()
                    .map {
                        it.body()
                    }
                    .onErrorResumeNext(localCatFactsGenerator.generateCatFact().toObservable())
            }
    }

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