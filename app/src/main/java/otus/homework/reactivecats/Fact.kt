package otus.homework.reactivecats

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Fact(
    @SerialName("fact")
    val text: String
)