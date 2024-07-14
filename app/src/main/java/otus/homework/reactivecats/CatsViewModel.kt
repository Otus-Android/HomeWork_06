package otus.homework.reactivecats

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.SocketTimeoutException


class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result<CatFactPresentation>>()
    val catsLiveData: LiveData<Result<CatFactPresentation>> = _catsLiveData

    private var job: Job? = null

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        println("Exception handler")
        //handleException(exception)
    }

    init {
        startFetchingCatFactsAndImages()
    }

    private fun startFetchingCatFactsAndImages() {
        job?.cancel()
        job = viewModelScope.launch(coroutineExceptionHandler) {
            while (isActive) {
                try {
                    val fact = withContext(Dispatchers.IO) {
                        catsService.getCatFact()
                    }
                    val catImage = withContext(Dispatchers.IO) {
                        RetrofitInstance.catImageService.getRandomCatImage().firstOrNull()
                    }
                    _catsLiveData.postValue(Result.Success(CatFactPresentation(fact.text, catImage?.url)))
                } catch (exception: Exception) {
                    handleException(exception)
                }
                delay(2000) // Delay for 2 seconds before fetching the next fact and image
            }
        }
    }

    private suspend fun handleException(exception: Throwable) {
        when (exception) {
            is SocketTimeoutException -> {
                showToast("Не удалось получить ответ от сервера")
            }
            is HttpException -> {
                showToast(exception.message ?: context.getString(R.string.default_error_text))
            }
            else -> {
                showToast(exception.message ?: context.getString(R.string.default_error_text))
            }
        }
        val fallbackFact = withContext(Dispatchers.IO) {
            localCatFactsGenerator.generateCatFact()
        }
        _catsLiveData.postValue(Result.Success(CatFactPresentation(fallbackFact.text, null)))
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}



