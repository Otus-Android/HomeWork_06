package otus.homework.reactivecats

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Flow
import java.util.concurrent.TimeUnit

class CatsViewModel(
    val catsService: CatsService,
    val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    var disposable: Disposable? = null

    init {
        disposable = getFacts()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { result ->
                Log.d("Reactive Cats", "New fact: ${result.text}")
                _catsLiveData.value = Success(result)
            }
    }

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }

    fun getFacts(): Flowable<Fact> = catsService.getCatFact()
        .delay(2000, TimeUnit.MILLISECONDS)
        .flatMap { response ->
            val newFlowable: Single<Fact>
            if (response.isSuccessful && response.body() != null) {
                newFlowable = Single.just(response.body()!!)
            } else {
                newFlowable = localCatFactsGenerator.generateCatFact()
            }
            newFlowable
        }
        .doOnError { it -> Log.e("Reactive Cats", "${it.message}") }
        .onErrorResumeNext(localCatFactsGenerator.generateCatFact())
        .repeat()
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