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
    service: ActivitiesService,
    localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    init {
        service.getActivity().enqueue(object : Callback<ActivityResponse> {
            override fun onResponse(call: Call<ActivityResponse>, response: Response<ActivityResponse>) {
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

            override fun onFailure(call: Call<ActivityResponse>, t: Throwable) {
                _catsLiveData.value = ServerError
            }
        })
    }

    fun getFacts() {}
}

class CatsViewModelFactory(
    private val catsRepository: ActivitiesService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: ActivityResponse) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()