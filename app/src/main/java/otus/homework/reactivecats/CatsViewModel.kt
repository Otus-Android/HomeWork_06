package otus.homework.reactivecats

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import otus.homework.reactivecats.network.CatsService
import java.util.concurrent.TimeUnit

class CatsViewModel(
	val catsService: CatsService,
	val localCatFactsGenerator: LocalCatFactsGenerator,
	context: Context,
	val schedulers: ISchedulers
) : ViewModel() {

	private val _catsLiveData = MutableLiveData<Result>()
	val catsLiveData: LiveData<Result> = _catsLiveData
	private var catsDisposable: Disposable? = null


	init {
		catsDisposable = getFacts()
			.subscribe(
				{ _catsLiveData.postValue(Success(it)) },
				{ _catsLiveData.postValue(Error(it.message ?: "")) }
			)
	}


	fun getFacts(): Flowable<Fact> {
		val source1 = catsService.getCatFact()
			.doOnSuccess { Log.d("лог", "получен факт из интернета - ${it.text}") }
		val source2 = localCatFactsGenerator.generateCatFactPeriodically()
			.doOnNext { Log.d("лог", "получен факт из локального источника - ${it.text}") }

		return source1
			.delay(2, TimeUnit.SECONDS)
			.repeat()
			.onErrorResumeNext(source2)



	}


	override fun onCleared() {
		catsDisposable?.dispose()
		super.onCleared()
	}
}

class CatsViewModelFactory(
	private val catsRepository: CatsService,
	private val localCatFactsGenerator: LocalCatFactsGenerator,
	private val context: Context,
	private val schedulers: ISchedulers
) :
	ViewModelProvider.NewInstanceFactory() {
	@Suppress("UNCHECKED_CAST")
	override fun <T : ViewModel?> create(modelClass: Class<T>): T =
		CatsViewModel(catsRepository, localCatFactsGenerator, context, schedulers) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()