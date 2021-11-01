package otus.homework.reactivecats

import android.content.Context
import androidx.annotation.StringRes

class ResourceWrapper(private val context: Context) {
    fun getString(@StringRes resId: Int): String = context.resources.getString(resId)
}