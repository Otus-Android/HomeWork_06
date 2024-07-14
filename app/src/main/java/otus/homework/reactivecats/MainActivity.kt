package otus.homework.reactivecats

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class MainActivity : AppCompatActivity() {

    private val diContainer = DiContainer()
    private val catsViewModel by viewModels<CatsViewModel> {
        CatsViewModelFactory(
            diContainer.service,
            diContainer.localCatFactsGenerator(applicationContext),
            applicationContext
        )
    }

    private lateinit var catsView: CatsView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        catsView =
            findViewById(R.id.catsView) // Assuming catsView is the correct ID for your CatsView in activity_main.xml

        catsViewModel.catsLiveData.observe(this) { result ->
            when (result) {
                is Result.Success -> catsView.populate(result.data)
                is Result.Error -> {
                    showToast(result.message)
                    // Optionally, handle additional error logic
                }
                Result.ServerError -> {
                    showToast("Server error occurred")
                    // Optionally, handle additional server error logic
                }

                else -> {
                    showToast("Unexpected result")
                }
            }
        }

    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    override fun onStop() {
        super.onStop()
        // No additional code needed for onStop() based on the provided requirements
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

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object ServerError : Result<Nothing>()
}

data class Success(val fact: Fact) : Result<Fact>()
data class Error(val message: String) : Result<Nothing>()
object ServerError : Result<Nothing>()
