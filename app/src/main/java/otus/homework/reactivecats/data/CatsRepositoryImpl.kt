package otus.homework.reactivecats.data

import io.reactivex.Single
import otus.homework.reactivecats.domain.CatsRepository
import java.io.IOException

class CatsRepositoryImpl : BaseRepository(), CatsRepository {

    override fun getCatFact(): Single<String> =
        apiResultHandler(netService.getCatFact()).let { result ->
            when (result) {
                is ApiResult.Success -> Single.create { result.data.text }
                is ApiResult.Error -> throw Throwable(message = result.message)
                ApiResult.ServerError -> throw IOException()
            }
        }
}