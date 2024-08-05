package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.Scheduler
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

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    val compositeDisposable = CompositeDisposable()
    init {
        compositeDisposable.add(catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
            {fact -> _catsLiveData.value = Success(fact)},
            {e -> _catsLiveData.value = Error(context.getString(R.string.default_error_text)) }
        )
            )
        getFacts()
    }

    override fun onCleared(){
        super.onCleared()
        compositeDisposable.dispose()
    }

    private fun getFacts() {
        compositeDisposable.add(Flowable.interval(2,TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())

            .flatMapSingle { catsService.getCatFact() }
            .onErrorResumeNext(localCatFactsGenerator.generateCatFactPeriodically())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
            {fact -> _catsLiveData.value = Success(fact)},
            {e -> _catsLiveData.value = Error(context.getString(R.string.default_error_text)) }
        )
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