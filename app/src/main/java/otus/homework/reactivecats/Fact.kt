package otus.homework.reactivecats

import com.google.gson.annotations.SerializedName

data class Fact(
    @field:SerializedName("fact")
    val text: String
)

// needed another dto because api i used is a bit different.
// But it works at least
data class Facts(
    @field:SerializedName("data")
    val facts: List<Fact>
)
