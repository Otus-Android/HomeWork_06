package otus.homework.reactivecats

import java.util.concurrent.Callable
import kotlin.random.Random

class CatFactsCallable(
    private val catFacts: Array<String>
    ) : Callable<Fact> {

    private var previous: String? = null

    override fun call(): Fact {
        while (true) {
            val fact = catFacts[Random.nextInt(5)]
            return if (fact != previous) {
                previous = fact
                Fact(fact)
            } else continue
        }
    }
}