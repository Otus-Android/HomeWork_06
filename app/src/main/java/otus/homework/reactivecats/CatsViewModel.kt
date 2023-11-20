package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Flow
import java.util.concurrent.Flow.Subscriber

class CatsViewModel(
    catsService: CatsService,
    localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val disposables = mutableListOf<Disposable>()

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData


    init {
        disposables.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _catsLiveData.value = Success(it)
                    },
                    {
                        _catsLiveData.value = Error(
                            it.message ?: context.getString(R.string.default_error_text)
                        )
                    }
                )
        )
    }

    fun getFacts() {}

    override fun onCleared() {
        disposables.onEach { it.dispose() }
    }

    companion object {
        val factory: (CatsService, LocalCatFactsGenerator, Context) -> ViewModelProvider.Factory =
            { catsRepository, localCatFactsGenerator, context ->
                viewModelFactory {
                    initializer {
                        CatsViewModel(catsRepository, localCatFactsGenerator, context)
                    }
                }
            }
    }
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
data object ServerError : Result()