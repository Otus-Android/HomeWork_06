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
    context: Context
) : ViewModel() {

  private val _catsLiveData = MutableLiveData<Result>()
  val catsLiveData: LiveData<Result> = _catsLiveData
  private val disposable = CompositeDisposable()

  init {
    disposable
      .add(getFacts()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          {
            _catsLiveData.value = Success(it)
          }, {
            _catsLiveData.value = Error(it.message ?: "something nasty happened")
          }
        ))
  }

  private fun getFacts(): Flowable<Fact> {
    return catsService
      .getCatFact()
      .delay(2000, TimeUnit.MILLISECONDS)
      .repeat()
      .onErrorResumeNext(
        localCatFactsGenerator.generateCatFactPeriodically()
      )
  }

  override fun onCleared() {
    disposable.dispose()
    super.onCleared()
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