package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : BaseViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val errorMessage = context.getString(R.string.default_error_text)

    init {
        catsService.getCatFact().execute(
            onSuccess = { _catsLiveData.value = Success(it) },
            onError = ::handleError
        )
    }

    fun getFacts() {
        Observable.interval(2, TimeUnit.SECONDS)
            .flatMapSingle {
                catsService.getCatFact()
                    .onErrorResumeNext(localCatFactsGenerator.generateCatFact())
            }
            .execute { _catsLiveData.value = Success(it) }
    }

    private fun handleError(e: Throwable) {
        if (e is HttpException) _catsLiveData.value = ServerError
        else e.message ?: errorMessage
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