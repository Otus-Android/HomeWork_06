package otus.homework.reactivecats

import io.reactivex.Scheduler

interface ISchedulers {
	fun io(): Scheduler
	fun ui():Scheduler
}