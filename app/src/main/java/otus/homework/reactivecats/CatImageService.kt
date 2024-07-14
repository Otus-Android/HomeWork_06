import retrofit2.http.GET

interface CatImageService {

    @GET("images/search")
    suspend fun getRandomCatImage(): List<CatImage>
}

data class CatImage(
    val id: String,
    val url: String,
    val width: Int,
    val height: Int
)
