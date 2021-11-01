package otus.homework.reactivecats

import io.reactivex.Scheduler

interface RxSchedulers {
    val mainThreadScheduler: Scheduler
    val ioScheduler: Scheduler
}