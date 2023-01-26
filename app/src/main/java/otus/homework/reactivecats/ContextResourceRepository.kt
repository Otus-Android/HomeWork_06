package otus.homework.reactivecats

import android.content.Context

class ContextResourceRepository(
    private val context: Context,
) : ResourceRepository {
    override fun getString(resId: Int): String {
        return context.resources.getString(resId)
    }

    override fun getStringArray(resId: Int): Array<String> {
        return context.resources.getStringArray(resId)
    }
}