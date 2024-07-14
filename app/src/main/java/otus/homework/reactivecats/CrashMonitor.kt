package otus.homework.reactivecats

import android.util.Log

object CrashMonitor {
    fun trackWarning(exception: Throwable) {
        Log.e("CrashMonitor", "Caught exception", exception)
    }

}
