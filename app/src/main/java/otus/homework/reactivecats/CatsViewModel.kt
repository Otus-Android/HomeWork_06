package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    val catsService: CatsService,
    val localCatFactsGenerator: LocalCatFactsGenerator,
    val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    val compositeDisposable = CompositeDisposable()

    init {
        getFacts()
    }


    fun getOneFact() {
        compositeDisposable.add(

            this.catsService
                .getCatFact()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    _catsLiveData.value = Success(it)
                }, {
                    _catsLiveData.value = Error(
                        it.message ?: context.getString(
                            R.string.default_error_text
                        )
                    )
                })
        )
    }

    fun getFacts() {

        compositeDisposable.add(
            Observable.interval(2000, TimeUnit.MILLISECONDS).flatMap { _ ->
                this.catsService.getCatFact()
                    .onErrorResumeNext {  this.localCatFactsGenerator.generateCatFact() }
                    .toObservable()
            }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    _catsLiveData.value = Success(it)
                }, {

                    _catsLiveData.value = Error(
                        it.message ?: this.context.getString(
                            R.string.default_error_text
                        )
                    )
                })
        )

    }

    override fun onCleared() {
        compositeDisposable.clear()
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