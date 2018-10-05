package com.lgo.launcher

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.*
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.util.Log
import android.view.*
import android.support.v4.view.GestureDetectorCompat
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import com.lgo.library.GestureListener
import com.lgo.library.XWalk.XView
import com.lgo.library.doAsync
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    private val TAG = "Main"
    private lateinit var mDetector: GestureDetectorCompat
    private lateinit var setting: SharedPreferences
    private lateinit var xWalkView: XView
    private lateinit var url: String

    init {
        instance = this
    }

    companion object {
        private var instance: MainActivity? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //Init var
        setting = PreferenceManager.getDefaultSharedPreferences(this)
        mDetector = GestureDetectorCompat(this, GestureListener())

        //Init setting
        initSetting()
//
//        val settingCount = 0
//        var index = 6





//        Thread(){
//
//            val handler = Handler(Looper.getMainLooper())
//
//            while (index > settingCount){
//                handler.post {
//                    textView.text = index.toString()
//                }
//                index--
//                Thread.sleep(1000)
//            }
//
//            handler.post {
//                xWalkView.show()
//                xWalkView.loadUrl(url)
//            }
//        }.start()

        xWalkView.show()
        xWalkView.loadUrl(url)

    }

    fun initSetting(){
        //Screen orientation setting
        val screenOrientationSetting = setting!!.getString("screen_orientation_list", "0")
        setRequestedOrientation(Integer.parseInt(screenOrientationSetting))

        //System UI setting
        if (setting!!.getBoolean("auto_fullscreen", false)){
            window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                Log.i(TAG, "visibility: " + visibility)
                if (visibility.equals(0)){
                    hideSystemUI()
                }
            }
            hideSystemUI()
        }

        //WebView Setting
        val viewGroup: ViewGroup = findViewById(R.id.layout) as ViewGroup
        xWalkView = XView(context = this, viewGroup = viewGroup)

        //Init application
        initApplication()
    }

    fun initApplication() {
        url = setting!!.getString("url", "http://miraway.vn")
        val appIndex = setting!!.getString("application_list", "0").toInt()
        val app = this.resources.getTextArray(R.array.pref_application_list_titles)

        when (app[appIndex]) {
            "None" -> {

            }

            "Digital Signage" -> {

            }

            "Kiosk" -> {
                url += "/device/#/kiosk"
            }
            "Screen" -> {
                url += "/device/#/screen"
            }
            "Feedback" -> {
                url += "/device/#/feedback"
            }
            "Counter" -> {
                url += "/app/#/counter"
            }
            else -> {
            }
        }
    }

    private val countToStart = 3
    private var count = 0
    private var startMillis: Long = 0

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {

        val action = ev?.action

        if (action == MotionEvent.ACTION_UP){
            //get system current milliseconds
            val time = System.currentTimeMillis()

            //if it is the first time, or if it has been more than 3 seconds since the first tap ( so it is like a new try), we reset everything
            if (startMillis == 0L || time - startMillis > 1000) {
                startMillis = time
                count = 1
            } else { //  time-startMillis< 3000
                count++
            }//it is not the first, and it has been  less than 3 seconds since the first

            if (count == countToStart) {
                startActivity(Intent(applicationContext(), SettingsActivity::class.java))
            }
            return false
        }

//        mDetector?.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

}
