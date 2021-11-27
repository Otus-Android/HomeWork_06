package otus.homework.reactivecats

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class CatsViewModel(
    catsService: CatsService,
    val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    private var disposable: Disposable
    val catsLiveData: LiveData<Result> = _catsLiveData


    init {
        Log.i("Viewmodel", "init viewmodel")
        disposable =
            catsService.getCatFact()
            //.onExceptionResumeNext (localCatFactsGenerator.generateCatFact().toObservable())
            .onExceptionResumeNext (getFacts().toObservable())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    result ->
                    val predict: Boolean = ((_catsLiveData.value !is Success)
                            || ((_catsLiveData.value is Success)
                            && (_catsLiveData.value as Success).fact != result))

                    if(predict) _catsLiveData.value = Success(result)
                    Log.i("Viewmodel ", "onNext")
                },
                { ex ->
                    val message = ex.message ?: context.getString(R.string.default_error_text)
                    Log.i("Viewmodel ex", message)
                    _catsLiveData.value = Error(message)
                }
            )
    }

    fun getFacts(): Flowable<Fact> {
        return localCatFactsGenerator.generateCatFactPeriodically()
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
