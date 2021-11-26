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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val dispasable = CompositeDisposable()

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
        catsService
            .getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::onSuccess, ::onError)
            .also { dispasable.add(it) }
    }

    override fun onCleared() {
        dispasable.dispose()
    }

    fun getFacts() = Flowable
        .interval(0, 2, TimeUnit.SECONDS)
        .flatMap { getCatFact() }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(::onSuccess, ::onError)
        .also { dispasable.add(it) }

    private fun getCatFact(): Flowable<Fact> =
        catsService
            .getCatFact()
            .toFlowable()
            .onErrorResumeNext(localCatFactsGenerator.generateCatFact().toFlowable())

    private fun onSuccess(fact: Fact) {
        _catsLiveData.value = Success(fact)
    }

    private fun onError(throwable: Throwable) {
        _catsLiveData.value = Error(
            throwable.message ?: context.getString(R.string.default_error_text)
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