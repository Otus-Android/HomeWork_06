package otus.homework.reactivecats

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable

class MainActivity : AppCompatActivity() {

    private val diContainer = DiContainer()
    private val catsViewModel by viewModels<CatsViewModel> {
        CatsViewModelFactory(
            diContainer.service,
            diContainer.localCatFactsGenerator(applicationContext),
            applicationContext
        )
    }

    private val activityDisposables = CompositeDisposable()

    private val rootView by lazy {
        layoutInflater.inflate(R.layout.activity_main, null) as CatsView
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(rootView)
    }

    override fun onStart() {
        super.onStart()
        catsViewModel
            .catFactsObservable
            .subscribe(rootView::populate)
            .addTo(activityDisposables)
    }

    override fun onStop() {
        activityDisposables.clear()
        super.onStop()
    }
}