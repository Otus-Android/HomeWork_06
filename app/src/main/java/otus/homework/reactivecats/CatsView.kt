package otus.homework.reactivecats

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

class CatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ICatsView {

    fun setOnClickListener(action: () -> Unit) {
        findViewById<Button>(R.id.button).setOnClickListener {
            action()
        }
    }

    override fun populate(response: ActivityResponse) {
        findViewById<TextView>(R.id.fact_textView).text = response.activityMessage
    }
}

interface ICatsView {

    fun populate(response: ActivityResponse)
}