package org.otonishi.loadimageselector

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewTreeObserver
import android.widget.*
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

class LoadingImageSelector : LinearLayout {

    private var mainLayout: FrameLayout? = null
    private var labelLayout: FrameLayout? = null
    private var progressBar: ProgressBar? = null
    private var selectorImageView: ImageView? = null
    private var labelTextView: TextView? = null

    private var state = State.Loading
    private var callback: LoadingImageSelectorInterface? = null
    private var delayShowMs = 0L

    interface LoadingImageSelectorInterface {
        fun startLoad(atEnd: () -> Unit)
    }

    private val clickListener = OnClickListener {
        onClick()
    }

    constructor(context: Context) : super(context) {
        settingBaseLayout()
        createDefaultLayout()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        settingBaseLayout()
        createDefaultLayout()

        context.obtainStyledAttributes(attrs, R.styleable.LoadingImageSelector).apply {

            val lisText = getString(R.styleable.LoadingImageSelector_lis_text)
            setText(lisText)

            val lisTextStyle = getInt(R.styleable.LoadingImageSelector_lis_text_style, 0)
            setTextStyle(lisTextStyle)

            val lisTextSize = getDimension(R.styleable.LoadingImageSelector_lis_text_size, 0f)
            setTextSize(lisTextSize)

            val lisTextColor = getResourceId(R.styleable.LoadingImageSelector_lis_text_color, -1)
            if (lisTextColor != -1) {
                setTextColor(lisTextColor)
            }

            val lisState = getInt(R.styleable.LoadingImageSelector_lis_state, -1)
            if (lisState != -1) {
                this@LoadingImageSelector.state = State.findStateByValue(lisState)
            }
            setState(this@LoadingImageSelector.state)

            val lisSelected = getBoolean(R.styleable.LoadingImageSelector_lis_selected, false)
            setSelectorSelected(lisSelected)

            val lisImageSelector =
                getResourceId(R.styleable.LoadingImageSelector_lis_image_selector, -1)
            if (lisImageSelector != -1) {
                setImage(lisImageSelector)
            }

            val lisImageSize = getDimension(R.styleable.LoadingImageSelector_lis_image_size, -1f)
            if (lisImageSize != -1f) {
                setImageSize(lisImageSize.toInt())
            }

            recycle()
        }
    }

    private fun settingBaseLayout() {
        orientation = VERTICAL
    }

    private fun createDefaultLayout() {
        mainLayout = FrameLayout(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        }

        labelLayout = FrameLayout(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
            visibility = View.GONE
        }

        progressBar = ProgressBar(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }

        }
        setCustomProgressDrawable(ContextCompat.getDrawable(context, R.drawable.lis_progress))

        selectorImageView = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
        }

        labelTextView = TextView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
        }

        mainLayout?.addView(progressBar)
        mainLayout?.addView(selectorImageView)
        labelLayout?.addView(labelTextView)

        addView(mainLayout)
        addView(labelLayout)

        setOnClickListener(clickListener)
    }

    private fun setTextStyle(value: Int) {
        setTextStyle(
            when (value) {
                0 -> Typeface.DEFAULT
                1 -> Typeface.DEFAULT_BOLD
                2 -> Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                else -> Typeface.DEFAULT
            }
        )
    }

    fun setTextStyle(tf: Typeface) {
        labelTextView?.typeface = tf
    }

    fun setText(text: String?) {
        labelTextView?.text = text
        labelLayout?.visibility = if (text.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    fun setTextColor(@ColorRes id: Int) {
        labelTextView?.setTextColor(ContextCompat.getColor(context, id))
    }

    fun setTextSize(size: Float) {
        labelTextView?.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
    }

    fun setState(state: State) {
        if (state != this.state) {
            this.state = state
        }
        when (state) {
            State.Loading -> load()
            State.Show -> show()
        }
    }

    fun setSelectorSelected(isSelectorSelected: Boolean) {
        selectorImageView?.isSelected = isSelectorSelected
    }

    fun setImage(@DrawableRes id: Int) {
        selectorImageView?.setImageDrawable(ContextCompat.getDrawable(context, id)!!)

        if (selectorImageView?.width == 0) {
            setImageSize()
        } else {
            setImageSize(selectorImageView?.width ?: 0)
        }
    }

    private fun setImageSize() {
        val instrumentsListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                selectorImageView?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                setImageSize(selectorImageView?.width ?: 0)
                return
            }
        }
        selectorImageView?.viewTreeObserver?.addOnGlobalLayoutListener(instrumentsListener)
    }

    fun setImageSize(imageSize: Int) {

        mainLayout?.layoutParams?.apply {
            this.width = imageSize
            this.height = imageSize
        }

        progressBar?.layoutParams?.apply {
            this.width = FrameLayout.LayoutParams.MATCH_PARENT
            this.height = FrameLayout.LayoutParams.MATCH_PARENT
        }

        selectorImageView?.layoutParams?.apply {
            this.width = FrameLayout.LayoutParams.MATCH_PARENT
            this.height = FrameLayout.LayoutParams.MATCH_PARENT
        }

        progressBar?.requestLayout()
        selectorImageView?.requestLayout()
    }

    private fun onClick() {
        if (selectorImageView != null && state == State.Show) {
            load()
            callback?.startLoad {
                Handler(Looper.getMainLooper()).postDelayed({
                    show()
                }, delayShowMs)
            }
        }
    }

    fun load() {
        state = State.Loading
        progressBar?.visibility = View.VISIBLE
        selectorImageView?.visibility = View.INVISIBLE
    }

    fun show() {
        state = State.Show
        progressBar?.visibility = View.INVISIBLE
        selectorImageView?.visibility = View.VISIBLE
    }

    fun setCallback(callback: LoadingImageSelectorInterface) {
        this.callback = callback
    }

    fun setCustomProgressDrawable(progressDrawable: Drawable?) {
        progressBar?.indeterminateDrawable = progressDrawable
    }

    fun setDelayShowMs(delayShowMs: Long) {
        if (delayShowMs >= 0L) {
            this.delayShowMs = delayShowMs
        }
    }

    enum class State(val value: Int) {
        Loading(0), Show(1);

        companion object {
            fun findStateByValue(value: Int): State {
                return values().find { type ->
                    type.value == value
                } ?: throw Exception("value:$value not found.")
            }
        }
    }
}