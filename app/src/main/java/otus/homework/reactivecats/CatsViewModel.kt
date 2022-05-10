package otus.homework.reactivecats

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.reactivestreams.Publisher
import java.io.IOException
import java.util.concurrent.Flow
import java.util.concurrent.TimeUnit

class CatsViewModel(
    catsService: CatsService,
    localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private var disposables = CompositeDisposable()
    init {
        getFacts(catsService, localCatFactsGenerator)
    }

    override fun onCleared() {
        disposables.dispose()
        super.onCleared()
    }

    @SuppressLint("CheckResult")
    fun getFacts(
        catsService: CatsService,
        localCatFactsGenerator: LocalCatFactsGenerator
    ) {

        val flowable = Flowable
            .interval(2, TimeUnit.SECONDS)
            .flatMap{
                    catsService.getCatFact()
            }
            .onErrorResumeNext( localCatFactsGenerator.generateCatFact().toFlowable() )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    _catsLiveData.value = Success(it)
                },{
                    _catsLiveData.value = Error("Не могу получить данные")
                })

        localCatFactsGenerator.generateCatFact().toFlowable()

        disposables.add(flowable)

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