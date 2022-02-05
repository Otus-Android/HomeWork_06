package otus.homework.coroutines

import android.content.Context
import androidx.annotation.StringRes

interface IResourceProvider {
    fun getString(@StringRes string: Int, vararg objects: Any?): String
}

class ResourceProvider(
    private val app: Context
) : IResourceProvider {

    override fun getString(string: Int, vararg objects: Any?): String {
        return if (objects.isEmpty()) {
            app.getString(string)
        } else {
            app.getString(string, *objects)
        }
    }
}