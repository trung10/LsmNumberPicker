package com.trungpd.LmNumberPicker

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.text.InputType
import android.text.Spanned
import android.text.method.NumberKeyListener
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.EditText
import android.widget.NumberPicker
import androidx.annotation.ColorInt
import com.trungpd.LmNumberPicker.R
import java.lang.reflect.Field


class LsmNumberPicker : NumberPicker {
    companion object {
        private const val DEFAULT_SEPARATOR_COLOR = Color.TRANSPARENT
        private const val DEFAULT_TEXT_COLOR = Color.BLACK
        private const val DEFAULT_TEXT_SIZE = 40
        private const val DEFAULT_TEXT_STYLE = Typeface.NORMAL
        private const val DEFAULT_VALUE = 4
        private const val MAX_VALUE = 10
        private const val DEFAULT_EDITABLE = false
        private const val DEFAULT_WRAPPED = false
    }

    private val onValueChangeListener =
        OnValueChangeListener { picker, oldVal, newVal ->
            Log.e("Trung", "oldVal data: $oldVal")
            Log.e("Trung", "newVal data: $newVal")

            picker?.value = if (newVal < oldVal)
                oldVal - stepCount
            else
                oldVal + stepCount
        }

    val format = Formatter { v ->
        Log.e("Trung", "format data: $v")
        Log.e("Trung", "NumberPicker value: $value")


        if (v % stepCount == 0) {
            "$v"
        } else if (v > stepCount && (v - stepCount) <= minValue){
             "$minValue"
         } else {
            if ( v < value) {
                "${v - stepCount + value - v}"
            } else {
                "${v + stepCount - (v - value)}"
            }
        }
    }

    //fun  getCurrentValue(): Int? = if (selectorIndexToStringCache != null) selectorIndexToStringCache?[] else null

    var stepCount: Int = DEFAULT_VALUE
        set(value) {
            field = value
        }

    var separatorColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            try {
                dividerField?.set(this, ColorDrawable(separatorColor))
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }

    var textColorNumberPicker: Int = DEFAULT_TEXT_COLOR
        set(@ColorInt value) {
            field = value
            //textColor = value
            updateTextAttributes()
        }

    var textStyle: Int = DEFAULT_TEXT_STYLE
        set(value) {
            field = value
            updateTextAttributes()
        }

    var textSize: Int = DEFAULT_TEXT_SIZE
        set(value) {
            field = value
            updateTextAttributes()
        }

    var editable: Boolean = DEFAULT_EDITABLE
        set(value) {
            field = value
            descendantFocusability =
                if (value) ViewGroup.FOCUS_AFTER_DESCENDANTS else ViewGroup.FOCUS_BLOCK_DESCENDANTS
        }

    var fontName: String? = null
        set(value) {
            field = value
            updateTextAttributes()
        }

    private val wheelField: Field by lazy {
        val selectorWheelPaintField =
            NumberPicker::class.java.getDeclaredField("mSelectorWheelPaint")
        selectorWheelPaintField.isAccessible = true
        selectorWheelPaintField
    }

    private val dividerField: Field? by lazy {
        var field: Field? = null
        val fields = NumberPicker::class.java.declaredFields
        for (f in fields) {
            if (f.name == "mSelectionDivider") {
                f.isAccessible = true
                field = f
                break
            }
        }
        field
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.NumberPicker, 0, 0)

        separatorColor =
            a.getColor(R.styleable.NumberPicker_lsmSeparatorColor, DEFAULT_SEPARATOR_COLOR)
        textColorNumberPicker =
            a.getColor(R.styleable.NumberPicker_lsmTextColor, DEFAULT_TEXT_COLOR)
        textSize = a.getDimensionPixelSize(R.styleable.NumberPicker_lsmTextSize, DEFAULT_TEXT_SIZE)
        textStyle = a.getInt(R.styleable.NumberPicker_lsmTextColor, DEFAULT_TEXT_STYLE)
        fontName = a.getString(R.styleable.NumberPicker_lsmFontname)
        editable = a.getBoolean(R.styleable.NumberPicker_lsmEditable, DEFAULT_EDITABLE)
        wrapSelectorWheel = a.getBoolean(R.styleable.NumberPicker_lsmWrapped, DEFAULT_WRAPPED)

        value = a.getInteger(R.styleable.NumberPicker_lsmDefaultValue, DEFAULT_VALUE)
        minValue = a.getInteger(R.styleable.NumberPicker_lsmMinValue, DEFAULT_VALUE)
        maxValue = a.getInteger(R.styleable.NumberPicker_lsmMaxValue, MAX_VALUE)
        stepCount = a.getInteger(R.styleable.NumberPicker_stepCount, MAX_VALUE)
        a.recycle()

        disableFocusability()
    }

    /**
     * Disable focusability of edit text embedded inside the number picker
     * We also override the edit text filter private attribute by using reflection as the formatter is still buggy while attempting to display the default value
     * This is still an open Google @see <a href="https://code.google.com/p/android/issues/detail?id=35482#c9">issue</a> from 2012
     */
    private fun disableFocusability() {

        inputText?.filters = arrayOf(InputTextFilter())

        setOnValueChangedListener(onValueChangeListener)

        setFormatter(format)
    }

    val inputText: EditText? by lazy {
        try {
            val f = NumberPicker::class.java.getDeclaredField("mInputText")
            f.isAccessible = true
            f.get(this) as EditText
        } catch (e: NoSuchFieldException) {
            // nothing to do, ignoring
            null
        } catch (e: IllegalAccessException) {
            // nothing to do, ignoring
            null
        } catch (e: IllegalArgumentException) {
            // nothing to do, ignoring
            null
        }
    }

    val selectorIndexToStringCache: IntArray? by lazy {
        try {

            val f = NumberPicker::class.java.getDeclaredField("mSelectorIndices")

            f.isAccessible = true
            f.get(this) as IntArray?
        } catch (e: NoSuchFieldException) {
            // nothing to do, ignoring
            null
        } catch (e: IllegalAccessException) {
            // nothing to do, ignoring
            null
        } catch (e: IllegalArgumentException) {
            // nothing to do, ignoring
            null
        } catch (e: SecurityException){
            null
        }
    }



    /**
     * Uses reflection to access text size private attribute for both wheel and edit text inside the number picker.
     */
    private fun updateTextAttributes() {
        val typeface = if (fontName != null) Typeface.createFromAsset(
            context.assets,
            "fonts/$fontName"
        ) else Typeface.create(Typeface.DEFAULT, textStyle)
        try {
            val wheelPaint = wheelField.get(this) as Paint
            wheelPaint.color = textColorNumberPicker
            wheelPaint.textSize = textSize.toFloat()
            wheelPaint.typeface = typeface

            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child is EditText) {
                    child.setTextColor(textColorNumberPicker)
                    child.setTextSize(
                        TypedValue.COMPLEX_UNIT_SP,
                        pixelsToSp(context, textSize.toFloat())
                    )
                    child.inputType =
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
                    child.typeface = typeface

                    invalidate()
                    break
                }
            }
        } catch (e: NoSuchFieldException) {
            // nothing to do, ignoring
        } catch (e: IllegalAccessException) {
            // nothing to do, ignoring
        } catch (e: IllegalArgumentException) {
            // nothing to do, ignoring
        }
    }

    private fun pixelsToSp(context: Context, px: Float): Float =
        px / context.resources.displayMetrics.scaledDensity


    val DIGIT_CHARACTERS = charArrayOf(
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    )

    /**
     * Filter for accepting only valid indices or prefixes of the string
     * representation of valid indices.
     */
    inner class InputTextFilter : NumberKeyListener() {
        // XXX This doesn't allow for range limits when controlled by a
        // soft input method!
        override fun getInputType(): Int {
            return InputType.TYPE_CLASS_TEXT
        }

        override fun getAcceptedChars(): CharArray {
            return DIGIT_CHARACTERS
        }

        override fun filter(
            source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int
        ): CharSequence {
            Log.e("Trung", "start:")
            for (i in selectorIndexToStringCache!!) {
                Log.e("Trung", "value $i")
            }



            val text =  source.subSequence(start, end).toString()
            return when {
                text == "-" || text.isEmpty() || dest.toString().length > maxValue.toString().length + 1 -> {
                    ""
                }
                else -> {
                    text
                }
            }
        }
    }
}