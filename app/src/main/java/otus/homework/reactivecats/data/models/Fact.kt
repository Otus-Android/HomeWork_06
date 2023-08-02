package otus.homework.reactivecats.data.models

import com.google.gson.annotations.SerializedName

/**
 * Модель факта о кошке
 *
 * @property text описание факта
 */
// https://alexwohlbruck.github.io/cat-facts/docs/endpoints/facts.html
data class Fact(
    @field:SerializedName("text")
    val text: String
)