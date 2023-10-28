package otus.homework.reactivecats.callAdapter

import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

class CatsCallAdapter<R>(
    private val mainAdapter: CallAdapter<R, *>
) : CallAdapter<R, Any> {

    override fun responseType(): Type {
        return mainAdapter.responseType()
    }

    override fun adapt(call: Call<R>): Any {
        val adaptedCall = mainAdapter.adapt(call)
//        return when (adaptedCall) {
//            is Single<*> -> {
//                adaptedCall.observeOn(Schedulers.io())
//            }
//            else -> {
//                adaptedCall
//            }
//        }
        return adaptedCall
    }
}