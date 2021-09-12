package otus.homework.reactivecats

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

//Single
fun <T> Single<T>.applyScheduler() =
    subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

//Observable
fun <T> Observable<T>.applyScheduler(): Observable<T> =
    subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())