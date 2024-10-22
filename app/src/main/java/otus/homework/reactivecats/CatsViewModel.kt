package otus.homework.reactivecats

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "facts"
    }

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    private val disposables = ArrayList<Disposable>()

    private val resources = context.resources

    init {
        getFact()
    }

    private fun getFact() {
        catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { _catsLiveData.value = Success(it) },
                { error ->
                    _catsLiveData.value = Error(
                        error.message ?: resources.getString(R.string.default_error_text)
                    )
                }
            ).also { disposables.add(it) }
    }

//    init {
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
//    }

    fun getFacts() {

        catsService.getCatFact()
            .subscribeOn(Schedulers.computation())
            .delay(2000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    Log.i(TAG, "remote fact: ${it.text}")
                    _catsLiveData.value = Success(it)
                    getFacts()
                },
                { error ->

                    disposables.clear()

                    val fallbackResult = localCatFactsGenerator
                        .generateCatFact()
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            {
                                Log.i(TAG, "error: ${error.message}")
                                _catsLiveData.value = Success(it)
                                getFacts()
                            },
                            {
                                Error(resources.getString(R.string.default_error_text))
                            }
                        )
                }
            ).also { disposables.add(it) }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.forEach { it.dispose() }
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

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()