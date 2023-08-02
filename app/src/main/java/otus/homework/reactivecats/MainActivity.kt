package otus.homework.reactivecats

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import otus.homework.reactivecats.presentation.CatsView
import otus.homework.reactivecats.presentation.CatsViewModel
import otus.homework.reactivecats.presentation.models.CatAction
import otus.homework.reactivecats.presentation.models.CatState
import otus.homework.reactivecats.utils.CustomApplication

/**
 * Базовая `Activity` с `custom view` информации о коте
 */
class MainActivity : AppCompatActivity() {

    private val catsViewModel by viewModels<CatsViewModel> { initViewModel() }

    private lateinit var view: CatsView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = layoutInflater.inflate(R.layout.activity_main, null) as CatsView
        setContentView(view)
        if (savedInstanceState == null) {
            catsViewModel.dispatch(CatAction.StartProcess)
        }

        initObservers()
    }

    private fun initObservers() {
        catsViewModel.uiState.observe(this) { state -> renderState(state) }
    }

    private fun renderState(state: CatState) {
        when (state) {
            CatState.Idle -> {}
            is CatState.Success -> view.populate(state.cat)
            is CatState.Error -> renderError(state)
        }
    }

    private fun renderError(state: CatState.Error) {
        if (!state.isShown) {
            Snackbar.make(view, state.message, 1000).show()
            catsViewModel.dispatch(CatAction.MarkErrorShown)
        }
    }

    private fun initViewModel() = with(CustomApplication.diContainer(this)) {
        CatsViewModel.provideFactory(repository, stringProvider, rxSchedulers)
    }
}