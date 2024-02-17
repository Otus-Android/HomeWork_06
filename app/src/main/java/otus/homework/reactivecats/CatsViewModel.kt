package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    val compositeDisposable = CompositeDisposable()

    init {
        val disposable = getFacts1()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { fact ->
                    println("mytag: +++ fact received")
                    _catsLiveData.value = Success(fact)
                },
                { throwable ->
                    _catsLiveData.value = Error(throwable?.message?:context.getString(R.string.default_error_text))
                }
            )
        compositeDisposable.add(disposable)
    }

    //Вопрос: при медленном интернет соединении посылается первый запрос, потом пауза в 6-10 сек,
    // а потом выплевывает сразу несколько значений. Как сделать так, чтоб поток ждал ответа от сервера?
    fun getFacts(): Observable<Fact> {
        val source = Observable.interval(2, TimeUnit.SECONDS)
            .flatMap {
                println("mytag: sending request")
                catsService.getCatFact().toObservable()
            }
            .onErrorResumeNext(
                localCatFactsGenerator.generateCatFactPeriodically()
            )

        return source
    }

    fun getFacts1(): Flowable<Fact> {
        // Вопрос: почему при создании interval не просить указать BackpressureStrategy?
        val source = Flowable.interval(2, TimeUnit.SECONDS)
            .flatMap {
                println("mytag: sending request")
                catsService.getCatFact().toFlowable()
            }
            .onErrorResumeNext(
                localCatFactsGenerator.generateCatFactPeriodically().toFlowable(BackpressureStrategy.BUFFER)
            )

        return source
    }

    fun onStop() {
        compositeDisposable.dispose()
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()