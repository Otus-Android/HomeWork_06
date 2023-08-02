package otus.homework.reactivecats.utils.rxjava

import io.reactivex.rxjava3.core.Scheduler

/**
 * Обертка получения `Scheduler`-ов
 */
interface RxSchedulers {

    /** `io Scheduler` */
    val io: Scheduler

    /** `main Scheduler` */
    val main: Scheduler
}