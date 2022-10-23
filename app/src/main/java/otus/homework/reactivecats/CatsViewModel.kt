package otus.homework.reactivecats

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import com.google.gson.JsonParseException
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Publisher
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

private const val TAG = "CatsViewModel"

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Application
) : AndroidViewModel(context) {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData;
    private val disposables = CompositeDisposable()

    private val catsPublisher: Publisher<Result> by lazy {
        val flowable = catsService.getCatFactUniversal()
            .map<Result> { Success(it) }
            .onErrorReturn { throwable: Throwable -> processException(throwable)}.toFlowable()
        return@lazy flowable
    }

    val catsLiveDataReactiveStream by lazy {
        LiveDataReactiveStreams.fromPublisher(catsPublisher)
    }

    val catsLiveDataReactiveStreamPeriodically: LiveData<Result> by lazy {
        val publisher: Publisher<Result> = getFacts()
            .map<Result> { Success(it) }
            .onErrorReturn { processException(it) }
        LiveDataReactiveStreams.fromPublisher<Result>(publisher)
    }

    init {
        disposables.add(catsService.getCatFactUniversal()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
            { fact -> _catsLiveData.value = Success(fact)},
            { throwable: Throwable -> _catsLiveData.value = processException(throwable) }
        ))
    }

    fun getFacts(): Flowable<Fact> {
        val flowable: Flowable<Fact>
        flowable = catsService
            .getCatFactUniversal()
            .doOnEvent { response, error ->
                Log.d(TAG, "onEvent has fact: ${response != null}, error: $error")
            }
            .onErrorResumeNext { throwable: Throwable ->
                Log.d(TAG, "onErrorReturn error: $throwable", throwable)
                return@onErrorResumeNext when (throwable) {
                    is HttpException,
                    is JsonParseException,
                    is IOException ->
                        localCatFactsGenerator.generateCatFact()
                    else -> throw throwable
                }
            }
            .doOnSuccess {
                if (Math.random() > 0.95) throw ReactiveTestException(context.getString(R.string.error_random))
            }
            .repeatUntil { Thread.sleep(Constants.PERIODIC_TIMEOUT_MS); false }
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged()
        return flowable
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

    private fun processException(throwable: Throwable): Result {
        return when (throwable) {
            is HttpException -> {
                val msg = throwable.response()?.errorBody()?.string() ?: context.getString(R.string.default_error_text)
                Error(msg)
            }
            is SocketTimeoutException -> Error(context.getString(R.string.error_socket_timeout))
            is UnknownHostException -> Error(throwable.toString())
            is ReactiveTestException -> Error(throwable.message)
            else -> ServerError
        }
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