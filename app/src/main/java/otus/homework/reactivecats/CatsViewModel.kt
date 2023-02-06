package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.HttpException
import java.net.SocketTimeoutException

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val _catsFlow = MutableStateFlow<otus.homework.reactivecats.Result>(Success(Fact(" ")))
    val catsFlow = _catsFlow.asStateFlow()

    private val compositeDisposable = CompositeDisposable()

    init {
        catsService.getFactAboutCats()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { fact -> _catsFlow.value = Success(fact) },
                { throwable ->
                    if (throwable.isServerError()) {
                        _catsFlow.value = ServerError
                    } else {
                        _catsFlow.value = Error(throwable.getErrorMessage())
                    }
                }
            ).also { compositeDisposable.add(it) }
    }


    private fun Throwable.isServerError(): Boolean {
        return this is SocketTimeoutException || this is HttpException
    }

    private fun Throwable.getErrorMessage(): String {
        return localizedMessage ?: message ?: context.getString(R.string.default_error_text)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    fun getFact(): Single<Fact> {
        return localCatFactsGenerator.generateCatFact()
    }
}



class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()
