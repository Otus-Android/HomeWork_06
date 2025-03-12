package otus.homework.reactivecats.data

import com.google.gson.annotations.SerializedName

data class Fact(
    @field:SerializedName("text")
    val text: String
)