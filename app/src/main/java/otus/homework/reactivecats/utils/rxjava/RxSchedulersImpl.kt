package otus.homework.reactivecats.utils.rxjava

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * Реализация обертки получения `Scheduler`-ов
 */
class RxSchedulersImpl : RxSchedulers {

    override val io: Scheduler = Schedulers.io()

    override val main: Scheduler = AndroidSchedulers.mainThread()
}