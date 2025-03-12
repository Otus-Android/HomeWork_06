package otus.homework.reactivecats

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.snackbar.Snackbar
import otus.homework.reactivecats.presentation.CatsView
import otus.homework.reactivecats.presentation.CatsViewModel
import otus.homework.reactivecats.presentation.CatsViewModelFactory
import otus.homework.reactivecats.presentation.Result.Error
import otus.homework.reactivecats.presentation.Result.ServerError
import otus.homework.reactivecats.presentation.Result.Success

class MainActivity : AppCompatActivity() {

    private val catsViewModel by viewModels<CatsViewModel> {
        CatsViewModelFactory(applicationContext)
    }

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = layoutInflater.inflate(R.layout.activity_main, null) as CatsView
        setContentView(view)
        catsViewModel.catsLiveData.observe(this) { result ->
            when (result) {
                is Success -> {
                    Toast.makeText (this, "It works", Toast.LENGTH_LONG).show()
                    view.populate(result.fact)
                }
                is Error -> {
                    Toast.makeText (this, result.message, Toast.LENGTH_LONG).show()
                    view.populate(result.fact)
                }
                ServerError -> Snackbar.make(view, "Network error", 1000).show()
            }
        }
    }
}