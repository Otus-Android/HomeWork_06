package otus.homework.reactivecats.utils

import android.app.Application
import android.content.Context
import otus.homework.reactivecats.di.DiContainer

/**
 * `Custom application`, предоставляющий контейнер зависимостей
 */
class CustomApplication : Application() {

    /** Контейнер зависимостей */
    lateinit var diContainer: DiContainer

    override fun onCreate() {
        super.onCreate()
        diContainer = DiContainer(this)
    }

    companion object {

        /** Получить контейнер зависимостей на основе контекста [Context] */
        fun diContainer(context: Context) =
            (context.applicationContext as CustomApplication).diContainer
    }
}