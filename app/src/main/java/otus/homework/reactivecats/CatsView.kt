package otus.homework.reactivecats

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout

class CatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ICatsView {
    override fun populate(uiState: CatFactUiState) {
        when (uiState) {
            is CatFactUiState.Error -> Toast.makeText(
                context,
                uiState.message,
                Toast.LENGTH_LONG
            ).show()

            CatFactUiState.Loading -> {
                findViewById<TextView>(R.id.fact_textView).text =
                    context.getString(R.string.loading_text)
            }

            is CatFactUiState.Success -> {
                findViewById<TextView>(R.id.fact_textView).text = uiState.fact.text
            }
        }

    }
}

interface ICatsView {

    fun populate(uiState: CatFactUiState)
}