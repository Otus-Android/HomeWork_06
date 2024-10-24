package otus.homework.reactivecats

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

@SuppressLint("CheckResult")
class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private var disposal: Disposable? = null
    private val defaultMessage = context.getString(R.string.default_error_text)

    fun getFacts() {
        disposal = Flowable.interval(2000L, TimeUnit.MILLISECONDS)
            .onBackpressureDrop()
            .subscribeOn(Schedulers.io())
            .flatMapSingle {
                catsService.getCatFact().onErrorResumeNext(
                        localCatFactsGenerator.generateCatFact()
                )
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe (
                { fact -> _catsLiveData.value = Success(fact) },
                { e -> _catsLiveData.value = Error(e.message ?: defaultMessage) }
            )
    }

    fun onDestroy() {
        disposal?.dispose()
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