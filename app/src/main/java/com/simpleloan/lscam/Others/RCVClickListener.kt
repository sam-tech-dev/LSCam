package com.simpleloan.lscam.Others

import android.view.View

interface RCVClickListener {
    abstract fun OnItemClick(clickView: View, position: Int)
}