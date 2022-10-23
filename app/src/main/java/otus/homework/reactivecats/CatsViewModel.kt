package otus.homework.reactivecats

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Publisher
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.util.concurrent.TimeUnit

private const val TAG = "CatsViewModel"

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Application
) : AndroidViewModel(context) {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData;
    var disposableSingleFact: Disposable? = null

    private val catsPublisher: Publisher<Result> by lazy {
        val flowable = catsService.getCatFactUniversal()
            .map<Result> { Success(it) }
            .onErrorReturn { throwable: Throwable ->
                if (throwable is HttpException) {
                    Error(
                        throwable.response()?.errorBody()?.string()
                            ?: context.getString(R.string.default_error_text)
                    )
                } else ServerError
            }.toFlowable()
        return@lazy flowable
    }

    val catsLiveDataReactiveStream by lazy {
        LiveDataReactiveStreams.fromPublisher<Result>(getFacts().map { Success(it) })
    }

    init {
        disposableSingleFact = catsService.getCatFactUniversal()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
            { fact -> _catsLiveData.postValue(Success(fact))},
            { throwable: Throwable? -> val error: Result = if (throwable is HttpException) {
                    Error(throwable.response()?.errorBody()?.string() ?: context.getString(R.string.default_error_text))
                } else ServerError
                _catsLiveData.postValue(error)
            }
        )
    }

    fun getFacts(): Flowable<Fact> {
        val flowable: Flowable<Fact>
        flowable = catsService.getCatFactUniversal()
            .repeatUntil { Thread.sleep(Constants.PERIODIC_TIMEOUT_MS); false }
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged()
        return flowable
//        return localCatFactsGenerator.generateCatFactPeriodically()
    }

    override fun onCleared() {
        super.onCleared()
        disposableSingleFact?.dispose()
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val application: Application
) : AndroidViewModelFactory(application) {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(CatsViewModel::class.java)) {
            throw IllegalArgumentException("Unsupported ViewModel class: " + modelClass.name)
        }
        @Suppress("UNCHECKED_CAST")
        return CatsViewModel(catsRepository, localCatFactsGenerator, application) as T
    }
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()