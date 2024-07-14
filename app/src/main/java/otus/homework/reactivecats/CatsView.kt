package otus.homework.reactivecats

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.squareup.picasso.Picasso

class CatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val textView: TextView
    private val imageView: ImageView

    init {
        inflate(context, R.layout.cats_view_layout, this)
        textView = findViewById(R.id.fact_textView)
        imageView = findViewById(R.id.cat_imageView)
    }

    fun populate(catFactPresentation: CatFactPresentation) {
        textView.text = catFactPresentation.factText

        // Load the cat image using Picasso
        catFactPresentation.imageUrl?.let {
            Picasso.get().load(it).into(imageView)
        }
    }
}
