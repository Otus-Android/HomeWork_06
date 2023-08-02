package otus.homework.reactivecats.utils

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes

/**
 * Поставщик строковых значений
 */
interface StringProvider {

    /** Получить строковое представление на основе идентификатора [res] */
    fun getString(@StringRes res: Int): String

    /** Получить массив строк на основе идентификатора [res] */
    fun getStringArray(@ArrayRes res: Int): Array<String>
}