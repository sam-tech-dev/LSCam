package com.simpleloan.lscam

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue
import java.text.SimpleDateFormat
import java.util.*

    object Utils {

        val TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss"

        val timestamp: String
            get() = SimpleDateFormat(TIMESTAMP_FORMAT, Locale.US).format(Date())


        fun dpToPx(context: Context, dip: Int): Int {
            val r = context.resources
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip.toFloat(),
                r.displayMetrics
            ).toInt()
        }


        fun getScreenWidth(context: Context): Int {
            val displayMetrics = DisplayMetrics()
            (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
            return displayMetrics.widthPixels
        }
    }