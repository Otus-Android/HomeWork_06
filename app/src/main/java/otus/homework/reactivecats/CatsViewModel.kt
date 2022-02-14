package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val disposables = CompositeDisposable()

    init {
        /*catsService.getCatFact().enqueue(object : Callback<Fact> {
            override fun onResponse(call: Call<Fact>, response: Response<Fact>) {
                if (response.isSuccessful && response.body() != null) {
                    _catsLiveData.value = Success(response.body()!!)
                } else {
                    _catsLiveData.value = Error(
                        response.errorBody()?.string() ?: context.getString(
                            R.string.default_error_text
                        )
                    )
                }
            }

            override fun onFailure(call: Call<Fact>, t: Throwable) {
                _catsLiveData.value = ServerError
            }
        })*/
        disposables.add(getFacts())
    }

    override fun onCleared() {
        disposables.dispose()
        super.onCleared()
    }

    private fun getFacts() : Disposable {
        return catsService.getCatFact()
            .repeatWhen{completed -> completed.delay(2, TimeUnit.SECONDS)}
            .onErrorResumeNext(localCatFactsGenerator.generateCatFactPeriodically())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _catsLiveData.value = Success(it)
            }, {
                _catsLiveData.value = Error(
                    it.message ?: context.getString(
                        R.string.default_error_text
                    )
                )
            })
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