package otus.homework.reactivecats

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val diContainer = DiContainer()
    private val catsViewModel by viewModels<CatsViewModel> {
        CatsViewModelFactory(
            diContainer.service,
            diContainer.localCatFactsGenerator(applicationContext),
            diContainer.subscriptions
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = layoutInflater.inflate(R.layout.activity_main, null) as CatsView
        setContentView(view)
        catsViewModel.catsLiveData.observe(this) { result ->
            when (result) {
                is Success -> view.populate(result.fact)
                is Error -> {
                    val error = result.message
                        ?: applicationContext.getString(R.string.default_error_text)

                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}