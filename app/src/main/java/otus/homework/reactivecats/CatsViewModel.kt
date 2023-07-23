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
    val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    init {
        val disposable = catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {fact -> _catsLiveData.value = Result.Success(fact)},
                    {throwable -> _catsLiveData.value = Result.Error(throwable.message ?: context.getString(R.string.default_error_text))})
        compositeDisposable.add(disposable)
    }

    fun getFacts() {
        val disposable = Flowable.interval(0,2, TimeUnit.SECONDS)
            .flatMap { catsService.getCatFact().toFlowable()}
            .onErrorResumeNext{_:Throwable -> localCatFactsGenerator.generateCatFact().toFlowable()}
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {fact -> _catsLiveData.value = Result.Success(fact)},
                {throwable -> _catsLiveData.value = Result.Error(throwable.message ?: context.getString(R.string.default_error_text))})

        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
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
