package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

val catsViewModelFactory = fun(
    catsRepository: CatsService,
    localCatFactsGenerator: LocalCatFactsGenerator,
    context: Context
): ViewModelProvider.Factory =
    viewModelFactory {
        initializer {
            CatsViewModel(catsRepository, localCatFactsGenerator, context)
        }
    }