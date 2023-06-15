package otus.homework.reactivecats

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CatsRepository(private val service: CatsService, private val localCatFactsGenerator: LocalCatFactsGenerator) {

    fun getFacts(): Observable<Fact> =
        service.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun getLocalFacts(): Single<Fact> =
        localCatFactsGenerator.generateCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun getLocalFactsPeriodically(): Flowable<Fact> =
        localCatFactsGenerator.generateCatFactPeriodically()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
}