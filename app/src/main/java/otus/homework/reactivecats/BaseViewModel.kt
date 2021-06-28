package otus.homework.reactivecats

import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

open class BaseViewModel : ViewModel() {

    private val disposables = CompositeDisposable()

    override fun onCleared() {
        disposables.dispose()
        super.onCleared()
    }

    protected fun <E> Single<E>.execute(onSuccess: (E) -> Unit) =
        execute(onSuccess, onError = {})

    protected fun <E> Single<E>.execute(
        onSuccess: (E) -> Unit,
        onError: (Throwable) -> Unit = { },
        onSubscribe: (Disposable) -> Unit = {},
        onFinished: () -> Unit = {}
    ) {
        observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(object : SingleObserver<E> {
                override fun onSuccess(t: E) {
                    onSuccess(t)
                    onFinished()
                }

                override fun onSubscribe(d: Disposable) {
                    disposables.add(d)
                    onSubscribe(d)
                }

                override fun onError(e: Throwable) {
                    onError(e)
                    onFinished()
                }
            })
    }
}