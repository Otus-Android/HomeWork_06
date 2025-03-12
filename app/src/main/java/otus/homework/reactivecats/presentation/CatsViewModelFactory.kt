package otus.homework.reactivecats.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import otus.homework.reactivecats.data.CatsRepositoryImpl
import otus.homework.reactivecats.domain.CatsInteractor

class CatsViewModelFactory(
    private val context: Context
) : ViewModelProvider.NewInstanceFactory() {

    private val catsRepository = CatsRepositoryImpl()
    private val catsInteractor = CatsInteractor(catsRepository)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(context, catsInteractor) as T
}