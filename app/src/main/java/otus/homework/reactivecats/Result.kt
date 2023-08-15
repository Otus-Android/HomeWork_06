package otus.homework.reactivecats

sealed class Result {
    data class Success(val fact: Fact) : Result()
    data class Error(val message: String) : Result()
    object ServerError : Result()
}
