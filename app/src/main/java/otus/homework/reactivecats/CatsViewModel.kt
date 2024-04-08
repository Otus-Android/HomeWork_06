package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class CatsViewModel(
    catsService: CatsService,
    localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val bag: CompositeDisposable = CompositeDisposable()

    init {
        catsService.getCatFact()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { fact ->
                    if (fact != null) {
                        _catsLiveData.value = Success(fact)
                    } else {
                        _catsLiveData.value = Error(
                            context.getString(R.string.default_error_text)
                        )
                    }
                },
                { _catsLiveData.value = ServerError }
            )
            .also {
                bag.add(it)
            }
    }

    override fun onCleared() {
        bag.clear()
        super.onCleared()
    }

    fun getFacts() {}
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