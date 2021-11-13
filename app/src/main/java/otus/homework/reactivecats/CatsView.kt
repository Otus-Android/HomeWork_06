package otus.homework.reactivecats

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

interface ICatsView {
    fun populate(fact: Fact)
}

class CatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ICatsView {

    private val factText by lazy { findViewById<TextView>(R.id.fact_textView) }

    override fun populate(fact: Fact) {
        factText.text = fact.text
    }
}
