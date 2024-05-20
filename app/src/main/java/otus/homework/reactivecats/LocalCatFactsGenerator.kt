package otus.homework.reactivecats

import android.content.Context
import android.widget.Toast
import io.reactivex.Flowable
import io.reactivex.Single
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class LocalCatFactsGenerator(
    private val context: Context
) {

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(): Single<Fact> {
        return Single.create<Fact?> { emitter ->
            val generatedFact = context.resources.getStringArray(R.array.local_cat_facts).random()
            if (emitter.isDisposed) {
                emitter.onSuccess(
                    Fact(
                    generatedFact
                )
                )
            } else {
                emitter.onError(RuntimeException("generateCatFactDisposedError"))
            }
        }
            .doOnError {
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        return Flowable.interval(2000, TimeUnit.MILLISECONDS)
            .map { generateCatFact().blockingGet() }
            .distinctUntilChanged()
            .delay(200, TimeUnit.MILLISECONDS)
    }
}