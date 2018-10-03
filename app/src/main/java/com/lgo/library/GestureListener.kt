package com.lgo.library

import android.app.Activity
import android.app.IntentService
import android.content.Intent
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import com.lgo.launcher.MainActivity
import com.lgo.launcher.SettingsActivity
import java.util.logging.Handler


class GestureListener: GestureDetector.SimpleOnGestureListener() {
    private val DEBUG_TAG = "Gestures"

    private var DoubleTapWait = false

    override fun onDown(event: MotionEvent): Boolean {
        Log.d(DEBUG_TAG, "onDown: " + event.toString())
        return true
    }

    override fun onFling(event1: MotionEvent, event2: MotionEvent,
                         velocityX: Float, velocityY: Float): Boolean {
        Log.d(DEBUG_TAG, "onFling: " + event1.toString() + event2.toString())
        return true
    }

    override fun onLongPress(e: MotionEvent?) {
        Log.d(DEBUG_TAG, "onLongPress: " + e.toString())

        if (DoubleTapWait){
            val setting = Intent(MainActivity.applicationContext(), SettingsActivity::class.java)
            MainActivity.applicationContext().startActivity(setting)
        }

        super.onLongPress(e)
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
//        Log.d(DEBUG_TAG, "onDoubleTapEvent: " + e.toString())
        return super.onDoubleTapEvent(e)
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        Log.d(DEBUG_TAG, "onDoubleTap: " + e.toString())

        DoubleTapWait = true

        android.os.Handler().postDelayed({
            DoubleTapWait = false
        }, 1000)

        return super.onDoubleTap(e)
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
//        Log.d(DEBUG_TAG, "onScroll")
        return super.onScroll(e1, e2, distanceX, distanceY)
    }

    override fun onShowPress(e: MotionEvent?) {
//        Log.d(DEBUG_TAG, "onShowPress: " + e.toString())
        super.onShowPress(e)
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
//        Log.d(DEBUG_TAG, "onSingleTapConfirmed: " + e.toString())
        return super.onSingleTapConfirmed(e)
    }

    override fun onContextClick(e: MotionEvent?): Boolean {
//        Log.d(DEBUG_TAG, "onContextClick: " + e.toString())
        return super.onContextClick(e)
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
//        Log.d(DEBUG_TAG, "onSingleTapUp: " + e.toString())
        return super.onSingleTapUp(e)
    }
}