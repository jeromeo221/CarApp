package com.bhoodz.carapp.ui

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "VehicleItemClickListen"

class VehicleItemClickListener(context: Context, recyclerView: RecyclerView, private val listener: OnVehicleItemClickListener)
    : RecyclerView.SimpleOnItemTouchListener() {

    interface OnVehicleItemClickListener {
        fun onVehicleItemClick(view: View, position: Int)
    }

    private val gestureDetector = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val childView = recyclerView.findChildViewUnder(e.x, e.y)
            return if (childView != null) {
                listener.onVehicleItemClick(childView, recyclerView.getChildAdapterPosition(childView))
                true
            } else {
                false
            }
        }
    })

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(e)
    }
}