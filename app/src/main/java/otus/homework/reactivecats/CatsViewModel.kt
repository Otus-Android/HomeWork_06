package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
   private val catsService: CatsService,
   private val localCatFactsGenerator: LocalCatFactsGenerator ,
   context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private lateinit var disposable: Disposable

    init {
        getFacts(context)
    }

    fun getFacts(context: Context) {
        disposable = catsService.getCatFact()
            .subscribeOn(Schedulers.io())//подписываемся в потоке io
            .observeOn(AndroidSchedulers.mainThread())//подписчик выполняет код в main
            .onErrorResumeNext { localCatFactsGenerator.generateCatFact() } // Single передает управление другому Single вместо Trowable
            .repeatWhen { it.delay (2000, TimeUnit.MILLISECONDS) }// переподписываемся каждые 2000 милисекунды
            .subscribe(
                {
                    _catsLiveData.value = Success(it)
                },
                {
                    _catsLiveData.value = Error(context.getString(R.string.default_error_text))
                }
            )

    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose() // отписываемся
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