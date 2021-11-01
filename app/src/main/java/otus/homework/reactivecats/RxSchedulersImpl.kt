package otus.homework.reactivecats

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class RxSchedulersImpl : RxSchedulers {
    override val mainThreadScheduler: Scheduler
        get() = AndroidSchedulers.mainThread()
    override val ioScheduler: Scheduler
        get() = Schedulers.io()
}