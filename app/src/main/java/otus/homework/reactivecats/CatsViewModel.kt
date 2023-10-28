package otus.homework.reactivecats

import android.content.Context
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
    localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val disposable = CompositeDisposable()

    init {
        getFacts(context)
    }

    private fun getFacts(context: Context) {
        disposable.add(catsService.getCatFact()
            .onErrorResumeNext { catsService.getCatFact() }
            .subscribeOn(Schedulers.io())
            .delay(DELAY_TIME, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .repeat()
            .subscribe(
                { fact ->
                    if (fact != null) {
                        _catsLiveData.value = Success(fact)
                    } else {
                        _catsLiveData.value = Error(context.getString(
                            R.string.default_error_text))
                    }
                },
                { tr ->
                    tr.printStackTrace()
                    _catsLiveData.value = ServerError
                }
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }

    companion object {
        private const val DELAY_TIME = 3500L
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            CatsViewModel::class.java -> {
                CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
            }

            else -> {
                error("unknown $modelClass")
            }
        }
    }
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()