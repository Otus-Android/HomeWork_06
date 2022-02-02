package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val container = CompositeDisposable()

    private fun getFacts() {

        val offlineCatsDisposable = localCatFactsGenerator.generateCatFactPeriodically().subscribeOn(Schedulers.io())
        val disposable = catsService.getCatFact().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).doOnError {
            val offlineDisposable = offlineCatsDisposable.subscribe (
                {fact ->
                    _catsLiveData.postValue(Success(fact))
                }, { e ->
                    _catsLiveData.postValue(Error(e.message?: context.getString(R.string.default_error_text)))
                }
            )
            container.add(offlineDisposable)
        }.subscribe(
            { fact ->
                _catsLiveData.postValue(Success(fact))
            },
            { e ->
                _catsLiveData.postValue(Error(e.message?: context.getString(R.string.default_error_text)))
            }
        )

        container.add(disposable)
    }

    fun onActivityDestroy(){
        container.dispose()
    }

    fun onActivityCreate() {
        getFacts()
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