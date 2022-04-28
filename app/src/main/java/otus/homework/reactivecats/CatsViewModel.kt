package otus.homework.reactivecats

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.operators.flowable.FlowableIgnoreElements
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CatsViewModel(
    catsService: CatsService,
    localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData


//
//    val dispose = class DisposableObserver<Fact>() {
//        override fun onSuccess(response: Fact) {
//            _catsLiveData.value = Success(response)
//        }
//
//        override fun onError(e: Throwable) {
//            _catsLiveData.value = Error(e.message.toString())
//        }
//    }

    init {

//        catsService.getCatFact()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(dispose)

        getFacts(catsService, localCatFactsGenerator)

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

    override fun onCleared() {
        //dispose.dispose()
        super.onCleared()
    }

    fun getFacts(
        catsService: CatsService,
        localCatFactsGenerator: LocalCatFactsGenerator
    ) {

        val dispose = object: DisposableSubscriber<Fact>() {

            @SuppressLint("CheckResult")
            override fun onError(e: Throwable) {
                localCatFactsGenerator.generateCatFact().subscribe { it -> _catsLiveData.value = Success(it) }
                //_catsLiveData.value = Success(.toString()) //Error(e.message.toString())
            }

            override fun onComplete() {
                _catsLiveData.value = Success(Fact("That's all!!"))
            }

            override fun onNext(t: Fact?) {
                _catsLiveData.value = t?.let { Success(it) }
            }
        }

//        localCatFactsGenerator
//            .generateCatFactPeriodically()
//            .subscribe(dispose)

        val flow = catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(dispose)
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