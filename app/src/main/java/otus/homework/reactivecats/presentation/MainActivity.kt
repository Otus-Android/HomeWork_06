package otus.homework.reactivecats.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.snackbar.Snackbar
import otus.homework.reactivecats.DiContainer
import otus.homework.reactivecats.R.layout
import otus.homework.reactivecats.utils.CatsViewModelFactory
import otus.homework.reactivecats.utils.Result.ServerError
import otus.homework.reactivecats.utils.Result.Success
import otus.homework.reactivecats.utils.Result.Error

class MainActivity : AppCompatActivity() {

    private val diContainer = DiContainer()
    private val catsViewModel by viewModels<CatsViewModel> {
        CatsViewModelFactory(
            diContainer.service,
            diContainer.localCatFactsGenerator(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = layoutInflater.inflate(layout.activity_main, null) as CatsView
        setContentView(view)
        catsViewModel.catsLiveData.observe(this) { result ->
            when (result) {
                is Success -> view.populate(result.fact)
                is Error -> Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                ServerError -> Snackbar.make(view, "Network error", 1000).show()
                else -> {}
            }
        }
    }
}
