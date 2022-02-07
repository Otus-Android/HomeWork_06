package otus.homework.reactivecats

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val  localCatFactsGenerator: LocalCatFactsGenerator,
    private val  context: Context
) : ViewModel() {
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private fun getFacts():Observable<Fact> {
        return Observable.interval(2000, TimeUnit.MILLISECONDS)
            .flatMap { catsService.getCatFact().toObservable() }
            .subscribeOn(Schedulers.io())
            .onErrorResumeNext(localCatFactsGenerator.generateCatFact().toObservable())
            .observeOn(AndroidSchedulers.mainThread())
    }
    init {
        compositeDisposable.add(
            getFacts()
                .subscribe({
                    if (it != null){
                        Log.d("CatsViewModel", " данные пришли успешно $it")
                        _catsLiveData.value = Success(it)
                    } else{
                        Log.d("CatsViewModel", " данные пришли успешно, но пусто $it")
                        _catsLiveData.value = Error( context.getString(R.string.default_error_text))
                    }
                }, {
                    Log.d("CatsViewModel", "ошибка $it")
                    _catsLiveData.value = ServerError
                }
        )
        )
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
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