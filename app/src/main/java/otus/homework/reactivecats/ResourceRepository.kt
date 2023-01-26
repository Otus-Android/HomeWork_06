package otus.homework.reactivecats

interface ResourceRepository {
    fun getString(resId: Int): String
    fun getStringArray(resId: Int): Array<String>
}