package otus.homework.reactivecats

data class FactReserveDto(val fact: String, val length: Int) {
    fun toFact(): Fact = Fact(fact)
}
