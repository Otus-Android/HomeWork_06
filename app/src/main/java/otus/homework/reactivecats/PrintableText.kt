package otus.homework.reactivecats

import android.content.Context

class PrintableText(private val context: Context) {

    fun getErrorOrDefault(text: String?) = text ?: context.getString(R.string.default_error_text)
}