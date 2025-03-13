package otus.homework.reactivecats.domain

import android.content.Context
import io.reactivex.Single
import otus.homework.reactivecats.data.LocalCatFactsGenerator

class CatsInteractor(
    private val catsRepository: CatsRepository
) {

    fun getCatFact(): Single<String> = catsRepository.getCatFact()
    fun getLocalCatFact(context: Context) =
        LocalCatFactsGenerator(context).generateCatFact()
    fun getLocalCatFactPeriodically(context: Context) =
        LocalCatFactsGenerator(context).generateCatFactPeriodically()
}