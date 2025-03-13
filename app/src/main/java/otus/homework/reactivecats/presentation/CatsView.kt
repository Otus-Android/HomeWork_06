package otus.homework.reactivecats.presentation

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import otus.homework.reactivecats.R

class CatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ICatsView {

    override fun populate(fact: String) {
        findViewById<TextView>(R.id.fact_textView).text = fact
    }
}

interface ICatsView {

    fun populate(fact: String)
}