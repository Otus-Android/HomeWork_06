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
import java.util.concurrent.TimeUnit

class CatsViewModel(
    catsService: CatsService,
    localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private var compositeDisposable: CompositeDisposable? = CompositeDisposable()

    init {
        subscribeFacts(
            catsService = catsService,
            localCatFactsGenerator = localCatFactsGenerator,
            context = context
        )
    }

    fun subscribeFacts(
        catsService: CatsService,
        localCatFactsGenerator: LocalCatFactsGenerator,
        context: Context
    ) {
        getFacts(
            sercive = catsService,
            localCatFactsGenerator = localCatFactsGenerator
        ).subscribe(
            { fact: Fact? ->
                setSuccessResult(fact = fact, context = context)
            },
            {
                /**
                 * Тут серверной ошибки быть не может, так как у нас установлен фоллбэк,
                 * поэтому показываем только ошибку об отсутствии локальных фактов
                 */
                setEmptyCatsErrorResult(context)
            }
        ).also { disposable ->
            compositeDisposable?.add(disposable)
        }
    }

    /**
     * Метод настроен так, что каждые 2 секунды ходит в сеть.
     * Если возникает ошибка, показывается тост об ошибке,
     * затем метод генерирует локальный факт.
     * Поход в сеть при этом не прекращается.
     * Как только соединение будет восстановлено,
     * факты с сервера начнут приходить, а тосты с ошибкой прекратятся.
     */
    fun getFacts(
        sercive: CatsService,
        localCatFactsGenerator: LocalCatFactsGenerator
    ): Observable<Fact?> {
        return Observable.interval(
            FACTS_INITIAL_REQUEST_DELAY,
            FACTS_PERIOD_REQUEST_DELAY,
            TimeUnit.SECONDS,
            Schedulers.io()
        )
            .flatMapSingle {
                sercive.getCatFact()
                    /** Main нужно установить именно тут, чтобы корректно показывался Toast */
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError { setServerErrorResult() }
                    .onErrorResumeNext(localCatFactsGenerator.generateCatFact())
            }
    }

    private fun setSuccessResult(fact: Fact?, context: Context) {
        if (fact != null) {
            _catsLiveData.value = Success(fact)
        } else {
            setEmptyCatsErrorResult(context)
        }
    }

    private fun setServerErrorResult() {
        _catsLiveData.value = ServerError
    }

    private fun setEmptyCatsErrorResult(context: Context) {
        val errorMessage = context.getString(R.string.default_error_text)
        _catsLiveData.value = Error(errorMessage)
    }

    override fun onCleared() {
        compositeDisposable?.dispose()
        compositeDisposable = null
        super.onCleared()
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