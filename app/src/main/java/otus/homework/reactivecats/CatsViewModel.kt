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
import java.util.concurrent.TimeUnit


class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

//    private val _catsLiveData = MutableLiveData<Result>()
//    val catsLiveData: LiveData<Result> = _catsLiveData

    private val compositeDisposable = CompositeDisposable()
    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData


    init {

        getFacts()

//        catsService.getCatFact().enqueue(object : Callback<Fact> {
//            override fun onResponse(call: Call<Fact>, response: Response<Fact>) {
//                if (response.isSuccessful && response.body() != null) {
//                    _catsLiveData.value = Success(response.body()!!)
//                } else {
//                    _catsLiveData.value = Error(
//                        response.errorBody()?.string() ?: context.getString(
//                            R.string.default_error_text
//                        )
//                    )
//                }
//            }
//
//            override fun onFailure(call: Call<Fact>, t: Throwable) {
//                _catsLiveData.value = ServerError
//            }
//        })
    }

    fun getFacts() {
        // Очищаем предыдущие подписки перед новым запросом
        compositeDisposable.clear()

        val disposable = io.reactivex.Observable.interval(0, 2, TimeUnit.SECONDS)
            .flatMapSingle { _ ->
                catsService.getCatFact()
                    .onErrorResumeNext { throwable: Throwable ->
                        if (throwable is java.io.IOException) {
                            localCatFactsGenerator.generateCatFact()
                        } else {
                            io.reactivex.Single.error(throwable)
                        }
                    }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { fact ->
                    _catsLiveData.value = Result.Success(Single.just(fact))
                },
                { error ->
                    _catsLiveData.value = Result.Error(error.message ?: "Error")
                }
            )

        compositeDisposable.add(disposable)
    }

    private fun handleError(throwable: Throwable) {
        when {
            throwable is java.net.ConnectException ||
                    throwable is java.net.SocketTimeoutException ||
                    throwable is java.net.UnknownHostException -> {
                // Ошибка сети - используем локальный генератор
                try {
                    val localFact = localCatFactsGenerator.generateCatFact()
                    _catsLiveData.value = Result.Success(localFact)
                } catch (e: Exception) {
                    _catsLiveData.value = Result.Error(
                        context.getString(R.string.default_error_text)
                    )
                }
            }
            throwable is retrofit2.HttpException -> {
                // HTTP ошибка
                _catsLiveData.value = Result.Error(
                    throwable.message ?: context.getString(R.string.default_error_text)
                )
            }
            else -> {
                _catsLiveData.value = Result.ServerError
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
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

sealed class Result {
    data class Success(val fact: Single<Fact>) : Result()
    data class Error(val message: String) : Result()
    data class Loading(val isLoading: Boolean) : Result() // Добавляем состояние загрузки
    object ServerError : Result()
}