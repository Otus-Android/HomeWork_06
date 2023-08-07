package otus.homework.reactivecats

sealed interface Result

class Success(val fact: Fact): Result
class ErrorResult(val message: String): Result
object ServerError: Result


enum class ErrorTypes{
    ERROR_RESULT,
    SERVER_ERROR
}
