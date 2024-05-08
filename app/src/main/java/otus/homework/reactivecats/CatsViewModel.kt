package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val catsFactSubscriber =
        catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { it ->
                    _catsLiveData.value = Success(it)
                },
                { error ->
                    _catsLiveData.value = Error(error.toString())
                }
            )

    private val flowableFacts = getFacts().subscribeOn(Schedulers.io())
        .subscribe { _catsLiveData.value = Success(it) }

    override fun onCleared() {
        super.onCleared()
        catsFactSubscriber.dispose()
        flowableFacts.dispose()
    }

    fun getFacts(): Observable<Fact> {
        return Observable.create<Fact> { emitter ->
            while(!emitter.isDisposed) {
                catsService.getCatFact()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { fact ->
                            emitter.onNext(fact)
                        },
                        {
                            localCatFactsGenerator.generateCatFact().subscribe { localFact ->
                                emitter.onNext(localFact)
                            }.dispose()
                        }
                    )
                TimeUnit.MILLISECONDS.sleep(2000)
            }
        }
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