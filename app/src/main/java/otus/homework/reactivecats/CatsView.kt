package otus.homework.reactivecats

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

const val TAG = "CatsView"

class CatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ICatsView {

    override fun populate(fact: Fact) {
        Log.d(TAG, "Fact get")
        findViewById<TextView>(R.id.fact_textView).text = fact.text
    }
}

interface ICatsView {
    fun populate(fact: Fact)
}