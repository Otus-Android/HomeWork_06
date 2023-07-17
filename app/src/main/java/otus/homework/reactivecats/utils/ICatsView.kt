package otus.homework.reactivecats.utils

import otus.homework.reactivecats.Fact

interface ICatsView {

    fun populate(fact: Fact)
}