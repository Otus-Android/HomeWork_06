package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
        val getFactsDisposable = getFacts().subscribe(
            { success -> _catsLiveData.value = Success(success) },
            { error -> _catsLiveData.value = Errors(error.toString()) }
        )
        compositeDisposable.add(getFactsDisposable)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }


    //первая реализация

//    fun getFactsInitial(): Single<Fact> {
//        return catsService.getCatFact()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//    }

    //вторая реализация с взятием локальных строк в случае ошибки api

    fun getFacts(): Flowable<Fact> {
        return Flowable
            .interval(2000, TimeUnit.MILLISECONDS)
            .flatMapSingle {
                catsService.getCatFact()
                    .onErrorResumeNext { localCatFactsGenerator.generateCatFact() }
            }
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
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
data class Errors(val message: String) : Result()
object ServerError : Result()