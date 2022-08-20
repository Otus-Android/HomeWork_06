package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context,
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val compositeDisposable = CompositeDisposable()

    /*  init {
          catsService.getCatFact().enqueue(object : Callback<Fact> {
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
          })
      }*/

    init {
        val disposable = catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _catsLiveData.value = Success(it)
            }, {
                _catsLiveData.value = ResultError(it.message)
            })
        compositeDisposable.add(disposable)
    }

    fun getFacts() {
       val disposableTwo = Flowable.interval(2, TimeUnit.SECONDS)
           .subscribeOn(Schedulers.io())
           .flatMapSingle { catsService.getCatFact()
               .onErrorResumeNext { localCatFactsGenerator.generateCatFact() }}
           .observeOn(AndroidSchedulers.mainThread())
           .subscribe({
               _catsLiveData.value = Success(it)
           }, {
               _catsLiveData.value = ResultError(it.message)
           })
        compositeDisposable.add(disposableTwo)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context,
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class ResultError(val message: String?) : Result()
object ServerError : Result()