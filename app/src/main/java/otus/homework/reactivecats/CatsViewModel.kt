package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {
    val catsObservable: Observable<out Result> by lazy{
        catsSubject
            //логика обработки ошибок из п.2 в п.4 не нужно и сам catsSubject не нужен в принципе
            .onErrorReturn {
                when(it){
                    is SocketTimeoutException -> ServerError
                    else -> Error(it.message ?: "")
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
    }
    //get() = getFacts().map{Success(it)}.observeOn(AndroidSchedulers.mainThread())
    private val catsSubject: PublishSubject<Result> = PublishSubject.create()
    init {
        getFacts()
            .subscribeOn(Schedulers.io())
            .map{Success(it)}
            .subscribe(catsSubject)
    }

    private fun getFacts(): Observable<Fact> {
        return catsService.getCatFact()
            .onErrorResumeNext(localCatFactsGenerator.generateCatFact().toObservable())
            .repeatWhen { it.delay(2, TimeUnit.SECONDS) }
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