package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Flow.Subscriber
import java.util.concurrent.TimeUnit

class CatsViewModel(
    val catsService: CatsService,
    val localCatFactsGenerator: LocalCatFactsGenerator,
    val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val disposable = CompositeDisposable()

    init {
       // getFacts()
        disposable.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { fact->
                        _catsLiveData.value = Success(fact)
                    },
                    { error ->
                        _catsLiveData.value = Error(error.localizedMessage)
                    }
                )
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
    fun getFacts() {
        disposable.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .onErrorResumeNext { localCatFactsGenerator.generateCatFact() }
                .delay(2, TimeUnit.SECONDS)
                .repeat()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { fact ->
                        _catsLiveData.value = Success(fact)
                    },
                    { throwable ->
                        _catsLiveData.value = Error(
                            throwable.message ?: context.getString(R.string.default_error_text)
                        )
                    }
                )
        )
    }
}
@Suppress("UNCHECKED_CAST")
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