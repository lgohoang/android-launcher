package com.lgo.library

import android.app.Application
import android.content.Context
import android.print.PrintAttributes
import android.print.PrintJob
import android.print.PrintManager
import android.webkit.WebView

class Printer {

    private var context: Context? = null
    private var printManager: PrintManager? = null
    private var printJob: PrintJob? = null

    constructor(context: Context){
        this.context = context
        this.printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
    }

    fun buildHTML(){

    }

    fun printWebView(webView: WebView){
        val jobName = "Document"
        val printAdapter =  webView.createPrintDocumentAdapter(jobName)
        printManager?.print(
                jobName,
                printAdapter,
                PrintAttributes.Builder().build()
        ).also {
            this.printJob = it
        }
    }

}