package otus.homework.reactivecats.utils

import otus.homework.reactivecats.Fact

sealed class Result{
    class Success(val fact: Fact) : Result()
    class Error(val message: String) : Result()
    object ServerError : Result()
}
