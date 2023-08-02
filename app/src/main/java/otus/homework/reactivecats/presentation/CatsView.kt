package otus.homework.reactivecats.presentation

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import otus.homework.reactivecats.R
import otus.homework.reactivecats.domain.models.Cat

/**
 * `Custom view` информации о коте
 */
class CatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ICatsView {

    private lateinit var textView: TextView

    override fun onFinishInflate() {
        super.onFinishInflate()
        textView = findViewById(R.id.fact_textView)
    }

    override fun populate(cat: Cat) {
        textView.text = cat.fact
    }
}

/**
 * Интерфейс взаимодействия с `View`
 */
interface ICatsView {

    /** Обновить данные о коте [Cat] */
    fun populate(cat: Cat)
}