package otus.homework.reactivecats.utils

import android.content.Context

/**
 * Реализация поставщика строковых значений [StringProvider]
 *
 * @param context `application context`
 */
class StringProviderImpl(
    private val context: Context
) : StringProvider {

    override fun getString(res: Int) = context.getString(res)

    override fun getStringArray(res: Int): Array<String> = context.resources.getStringArray(res)
}