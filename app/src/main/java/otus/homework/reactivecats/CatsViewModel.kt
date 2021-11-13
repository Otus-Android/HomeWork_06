package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

    init {
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
    }

    fun getFacts() {}
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()
