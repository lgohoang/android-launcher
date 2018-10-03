package com.lgo.library

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import android.webkit.WebView
import android.content.ContextWrapper
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebViewClient


class XView {

    enum class Engine {
        Build_In, CrossWalk
    }

    private var xWebView: XWebView? = null
    private var engine: Engine = Engine.Build_In

    init {
        WebView.setWebContentsDebuggingEnabled(true)
    }

    constructor(context: Context, viewGroup: ViewGroup?, engine: Engine){

        this.engine = engine

        if (this.engine.equals(Engine.Build_In)){
//            xWebView = XWebView(context, viewGroup)
        }
    }

    fun loadUrl(url: String?){
        if (engine.equals(Engine.Build_In)){
            url?.let { xWebView?.loadUrl(it) }
        }
    }

    fun getActivity(context: Context?): Activity? {
        if (context == null) return null
        if (context is Activity) return context
        return if (context is ContextWrapper) getActivity(context.baseContext) else null
    }
}