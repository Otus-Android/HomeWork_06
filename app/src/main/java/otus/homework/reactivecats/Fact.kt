package otus.homework.reactivecats

import com.google.gson.annotations.SerializedName

data class Fact(
    @SerializedName("text")
    val text: String
)