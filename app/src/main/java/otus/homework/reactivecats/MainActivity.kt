package otus.homework.reactivecats

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

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
        val view = layoutInflater.inflate(R.layout.activity_main, null) as CatsView
        setContentView(view)
        catsViewModel.catsLiveData.observe(this) { result ->
            when (result) {
                is Success -> view.populate(result.fact)
                is Error -> toast(result.message ?: getString(R.string.default_error_text))
                ServerError -> Snackbar.make(view, "Network error", 1000).show()
            }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}