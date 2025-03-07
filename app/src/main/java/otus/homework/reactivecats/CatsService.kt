package otus.homework.reactivecats

import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import retrofit2.http.GET

data class JsonBinRecord<T>(
    @field:SerializedName("record")
    val record: T?,
    @field:SerializedName("metadata")
    val metadata: JsonBinMetadata?,
)

data class JsonBinMetadata(
    @field:SerializedName("id")
    val id: String?,
    @field:SerializedName("private")
    val private: Boolean?,
    @field:SerializedName("createdAt")
    val createdAt: String?,
    @field:SerializedName("name")
    val name: String?,
)

interface CatsService {
    //@GET("random?animal_type=cat")
    @GET("b/67caaccbe41b4d34e4a246d")
    fun getCatFact(): Single<JsonBinRecord<Fact>>
}