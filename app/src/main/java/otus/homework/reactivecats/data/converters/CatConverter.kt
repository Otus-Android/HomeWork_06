package otus.homework.reactivecats.data.converters

import otus.homework.reactivecats.data.models.Fact
import otus.homework.reactivecats.domain.models.Cat

/**
 * Конвертер данных из [Fact] в данные с информацией о кошке [Cat]
 */
class CatConverter {

    /** Сконвертировать факт [Fact] в информацию о кошке [Cat] */
    fun convert(fact: Fact) = Cat(fact = fact.text)
}