package otus.homework.reactivecats

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
    private val compositeDisposable = CompositeDisposable()

    init {
        getFacts(
            catsService = catsService,
            localCatFactsGenerator = localCatFactsGenerator
        )
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("${javaClass.name}#onCleared()", "")
        compositeDisposable.dispose()
    }

    private fun getFacts(
        catsService: CatsService,
        localCatFactsGenerator: LocalCatFactsGenerator
    ) {
        compositeDisposable.add(
            catsService.getCatFact()
                .onErrorResumeNext {
                    Log.d("${javaClass.name}#onErrorResumeNext", "Some problem with catService")
                    localCatFactsGenerator.generateCatFact()
                }
                .delay(2000, TimeUnit.MILLISECONDS)
                .repeat()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { fact ->
                        Log.d("${javaClass.name}#onNext", fact.toString())
                        _catsLiveData.value = Success(fact)
                    },
                    { e ->
                        Log.d("${javaClass.name}#onError", e.toString())
                        _catsLiveData.value = Error(e.message.toString())
                    }
                )
        )
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