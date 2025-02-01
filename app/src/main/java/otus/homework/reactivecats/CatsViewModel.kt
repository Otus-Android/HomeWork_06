package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.*
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
        getFacts()
    }

    fun getFacts() {
        val source = Flowable.interval(2000, TimeUnit.MILLISECONDS)

        val disposable = source.subscribe({
            val innerDisposable = catsService.getCatFact().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).repeat()
                .subscribe({ fact -> _catsLiveData.value = Success(fact) }, {
                    val secondInnerDisposable =
                        localCatFactsGenerator.generateCatFact().subscribe({ fact ->
                                _catsLiveData.value = Success(fact)
                            }, { error ->
                                _catsLiveData.value = error.message?.let { return@let Error(it) }
                                    ?: ServerError
                            })
                    compositeDisposable.add(secondInnerDisposable)
                })
            compositeDisposable.add(innerDisposable)
        }, { error ->
            _catsLiveData.value = error.message?.let { return@let Error(it) } ?: ServerError
        })

        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
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