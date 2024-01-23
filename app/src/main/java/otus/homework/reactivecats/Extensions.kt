package otus.homework.reactivecats

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

infix fun <T> T?.or(default: T) = this ?: default
fun Disposable.addTo(disposables: CompositeDisposable) = disposables.add(this)