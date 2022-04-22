package otus.homework.reactivecats

import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable


open class BaseViewModel : ViewModel(), LifecycleEventObserver {

    protected val compositeDisposable = CompositeDisposable()

    private fun onPause() {
        compositeDisposable.clear()
    }

    @CallSuper
    override fun onCleared() {
        super.onCleared()

        compositeDisposable.dispose()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_PAUSE -> onPause()
            else -> {}
        }
    }
}