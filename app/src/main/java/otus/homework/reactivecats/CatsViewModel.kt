package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit.MILLISECONDS

class CatsViewModel(
    private val catsService: CatsService,
    localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private var catFactRequestSubscription: Disposable? = null

    init {
        getFacts(localCatFactsGenerator, context)
    }

    private fun getFacts(localCatFactsGenerator: LocalCatFactsGenerator, context: Context) {
        catFactRequestSubscription = catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .onErrorResumeNext { localCatFactsGenerator.generateCatFact() }
            .repeatWhen { it.delay(2000, MILLISECONDS) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    _catsLiveData.value = Success(it)
                },
                {
                    _catsLiveData.value = Error(it.message ?: context.getString(R.string.default_error_text))
                }
            )
    }

    override fun onCleared() {
        super.onCleared()
        catFactRequestSubscription = null
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