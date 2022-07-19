package otus.homework.reactivecats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.concurrent.TimeUnit.MILLISECONDS

class CatsViewModel(
  private val catsService: CatsService,
  private val localCatFactsGenerator: LocalCatFactsGenerator,
  private val app: Application
) : AndroidViewModel(app) {

  private val _catsLiveData = MutableLiveData<Result>()
  val catsLiveData: LiveData<Result> = _catsLiveData

  private val compositeDisposable = CompositeDisposable()
  private val errorMessage
    get() = app.resources.getString(R.string.default_error_text)

  init {
    compositeDisposable.add(
      catsService.getCatFact()
        .subscribeOn(Schedulers.io())
        .subscribe(
          { _catsLiveData.postValue(Success(it)) },
          {
            when (it) {
              is IOException -> _catsLiveData.postValue(ServerError)
              else -> _catsLiveData.postValue(Error(it.message ?: errorMessage))
            }
          })
    )
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }

  fun getFacts() {
    compositeDisposable.add(
      Observable.interval(0, 2000, MILLISECONDS).flatMapSingle {
        catsService.getCatFact()
      }.onErrorResumeNext(localCatFactsGenerator.generateCatFact().toObservable())
        .subscribeOn(Schedulers.io())
        .subscribe(
          { _catsLiveData.postValue(Success(it)) },
          {
            when (it) {
              is IOException -> _catsLiveData.postValue(ServerError)
              else -> _catsLiveData.postValue(Error(it.message ?: errorMessage))
            }
          })
    )
  }
}

class CatsViewModelFactory(
  private val catsRepository: CatsService,
  private val localCatFactsGenerator: LocalCatFactsGenerator,
  private val application: Application
) :
  ViewModelProvider.NewInstanceFactory() {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel?> create(modelClass: Class<T>): T =
    CatsViewModel(catsRepository, localCatFactsGenerator, application) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()