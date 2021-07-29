package otus.homework.reactivecats

import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.Observer
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

    protected fun <E> Single<E>.execute(
        onSuccess: (E) -> Unit,
        onError: (Throwable) -> Unit = { }
    ) {
        observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(object : SingleObserver<E> {
                override fun onSuccess(t: E) {
                    onSuccess(t)
                }

                override fun onSubscribe(d: Disposable) {
                    disposables.add(d)
                }

                override fun onError(e: Throwable) {
                    onError(e)
                }
            })
    }

    protected fun <E> Observable<E>.execute(onNext: (E) -> Unit) = execute(onNext, onError = {})

    protected fun <E> Observable<E>.execute(
        onNext: (E) -> Unit,
        onError: (Throwable) -> Unit = { }
    ) {
        observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(object : Observer<E> {
                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {
                    disposables.add(d)
                }

                override fun onNext(t: E) {
                    onNext(t)
                }

                override fun onError(e: Throwable) {
                    onError(e)
                }
            })
    }
}