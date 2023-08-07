package otus.homework.reactivecats

import android.util.Log
import io.reactivex.Flowable

fun Flowable<Fact?>.getResult(errorType: ErrorTypes): Flowable<Result> {

    return map { (Success(it)) as Result }
        .onErrorResumeNext { exc: Throwable ->
            map {
                when (errorType) {
                    ErrorTypes.ERROR_RESULT -> {
                        Log.w("CatsError!", exc.message.toString())
                        ErrorResult(exc.message.toString())

                    }
                    ErrorTypes.SERVER_ERROR -> {
                        Log.w("CatsError!", "Server Error")
                        ServerError
                    }
                }
            }
        }
}


