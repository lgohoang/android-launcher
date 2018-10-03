package com.lgo.library

import android.content.Context
import android.net.Uri
import android.nfc.Tag
import android.text.BoringLayout
import android.util.Log
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import org.jsoup.helper.HttpConnection
import java.net.CacheRequest
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Paths
import com.lgo.library.Shared
import java.io.*
import org.xwalk.core.*
import java.lang.reflect.Field
import java.net.URLConnection
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.util.stream.Stream
import java.util.stream.StreamSupport
import javax.net.ssl.HttpsURLConnection
import android.webkit.MimeTypeMap
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.http.HttpMethod
import org.xwalk.core.XWalkWebResourceRequest
import org.xwalk.core.XWalkWebResourceResponse
import org.xwalk.core.XWalkResourceClient
import org.xwalk.core.XWalkView
import kotlin.concurrent.thread


class Cache {
    lateinit var storage: Storage
    private lateinit var context: Context
    private val TAG = "CACHE"
    private val mediaFolder = "media"
    private val tempFolder = "temp"
    private var caching = mutableMapOf<String, String>()

    private lateinit var mediaFiles: ArrayList<String>

    constructor(context: Context){
        this.context = context
        this.storage = Storage(context, Storage.Location.Internal)

        mediaFiles = ArrayList()

        mediaFiles.add("mp4")
//        mediaFiles.add("jpg")
//        mediaFiles.add("gif")
//        mediaFiles.add("png")
    }

    fun isFile(file: String): Boolean{
        val fileName = URLUtil.guessFileName(file, null, null)

        if (fileName.contains(".bin")){
            return false
        }

        return true
    }

    fun isMediaFile(file: String): Boolean {
        val fileName = URLUtil.guessFileName(file, null, null)
        mediaFiles.forEach {
            if (it.contains(File(fileName).extension, true)){
                return true
            }
        }
        return false
    }

    private fun removeNonAlphanumbericChar(input: String): String{
        val regex = Regex("[^A-Za-z0-9]")
        return regex.replace(input, "_")
    }

    fun buildFileFromUrl(uri: Uri): File{
        //root dir
        var rootDir = uri.host.toString()

        if (uri.port != null){
            rootDir += "_" + uri.port.toString()
        }

        //path
        val path = uri.path.toString()

        val dirPath = path.substring(0, path.lastIndexOf("/"))

        //file
        var filename = Shared.MD5(uri.toString())

        val ext = File(path).extension

        if (ext != ""){
            filename += ".$ext"
        }else{
            filename += ".xc"
        }

        return File(removeNonAlphanumbericChar(rootDir), File(dirPath, filename).toString())
    }

    fun buildMediaFilePath(uri: Uri): File {
        return File(mediaFolder, buildFileFromUrl(uri).toString())
    }

    fun buildTempFilePath(uri: Uri): File{
        return File(tempFolder, buildFileFromUrl(uri).toString())
    }

    private fun cacheOnInputStream(file: File, inputStream: InputStream): InputStream? {


//
//        val connection = URL(request.url.toString()).openConnection()

//        Log.i(TAG, "Res Header: " + connection.headerFields.toString())

        var fileOutputStream: OutputStream? = null

//        val inputStream = connection.getInputStream()

        var circularByteBuffer = CircularByteBuffer(-1, true)

        Thread().run(){

            while (true){

                try {

                    val bytes = ByteArray(1000)

                    var length = inputStream.read(bytes, 0, bytes.size)



                    if (length < 0){
                        break
                    }

                    if (fileOutputStream == null){
                        fileOutputStream = storage.writeFileOutputStream(file)
                    }

//                    while (circularByteBuffer.getSpaceLeft() < length){
//                        Thread.sleep(1000)
//                    }

                    circularByteBuffer.getOutputStream().write(bytes,0 , length)

                    fileOutputStream?.write(bytes, 0, length)




                }catch (ex: Exception){
                    Log.e(TAG, ex.toString())
                }

                Thread.sleep(10)
            }

            fileOutputStream?.close()
            circularByteBuffer.getOutputStream().close()
        }

        return circularByteBuffer.getInputStream()
    }

    private fun cacheOnPipedInputStream(file: File, inputStream: InputStream): InputStream? {

//        val pipedOutputStream = PipedOutputStream()
//        val pipedInputStream = PipedInputStream(pipedOutputStream, 999999)

        var circularByteBuffer = CircularByteBuffer(160000, true)

        Log.i(TAG, "Cache File $file Start")

        Thread().run(){
            Log.i(TAG, "Thread Cache ID " + this.id + " Start")
            while (true){
                val buffer = ByteArray(1000)
                val length = inputStream.read(buffer, 0, buffer.size)

                if (length < 0){
                    break
                }

                Log.i(TAG, "Start Write $length Byte Available: " + circularByteBuffer.getInputStream().available())

//                while (circularByteBuffer.getInputStream().available() >= length){
//                    Log.i(TAG, "Sleep $length - ${circularByteBuffer.getInputStream().available()}")
//                    Thread.sleep(500)
//                }

                circularByteBuffer.getOutputStream().write(buffer, 0, length)
                Log.i(TAG, "End Write $length Byte")

            }

            circularByteBuffer.getOutputStream().close()
            Log.i(TAG, "Thread Cache ID " + this.id + " End")
        }

        return circularByteBuffer.getInputStream()
    }

    fun cacheFirst(request: WebResourceRequest) {
        val tempFile = buildTempFilePath(Uri.parse(request.url.toString()))
        val file = buildMediaFilePath(Uri.parse(request.url.toString()))

//        val inputStream = URL(request.url.toString()).openStream()

        var key = Shared.MD5(request.url.toString())

        if(caching.containsKey(key)){
            return
        }

        caching.put(key, "file")


        Log.i(TAG, "Start cache url: ${request.url} \nTemp file: $tempFile")



//        val thread = Thread(){

            storage.download(request.url.toString(), tempFile)

            storage.move(tempFile, file)

            caching.remove(key)

            Log.i(TAG, "Cache done url: ${request.url} \nMove $tempFile to $file")

//        }.start()
    }


    private fun fallBack(request: WebResourceRequest): WebResourceResponse? {

        if (request.method.equals("POST")){
            return null
        }

        Log.i(TAG, "fallback mode: " + request.url)

        val connection = URL(request.url.toString()).openConnection()

        val file = buildFileFromUrl(request.url)
        var webResourceResponse: WebResourceResponse? = null
//        val header = request.requestHeaders



        try {
            webResourceResponse = WebResourceResponse(
                    "", "",
                    cacheOnInputStream(file, connection.getInputStream()))
            webResourceResponse?.responseHeaders?.put("Status", "Forward & Cache down storage")
        }catch (ex: Exception){
            Log.i(TAG, "connection exception: " + ex.toString())
//            header.put("Status", "Fallback from memory")
            webResourceResponse = WebResourceResponse(
                    "" , "",
                    storage.readFileInputStream(file))

        }finally {
            return webResourceResponse
        }
    }

    fun getMimeType(url: String): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }


//        if (extension.equals("mp4")){
//            type = "application/octet-stream"
//        }

        Log.i(TAG, "Ext: " + extension + " Type: " + type)

        return type
    }

    fun pipedInputStream(inputStream: InputStream, length: Int): InputStream{
        val pipedInputStream = object : PipedInputStream(){

            override fun close() {
                super.close()
            }

            override fun available(): Int {
                return length
            }

            override fun read(): Int {
                return super.read()
            }

            override fun read(b: ByteArray?): Int {
                return super.read(b)
            }

            override fun read(b: ByteArray?, off: Int, len: Int): Int {
                return super.read(b, off, len)
            }

            override fun receive(b: Int) {
                super.receive(b)
            }

            override fun connect(src: PipedOutputStream?) {
                super.connect(src)
            }

            override fun mark(readlimit: Int) {
                super.mark(readlimit)
            }

            override fun markSupported(): Boolean {
                return super.markSupported()
            }
        }
        val pipedOutputStream = PipedOutputStream(pipedInputStream)

        Thread(){
            pipedInputStream.use { _ ->
                pipedOutputStream.use { _ ->
                    pipedInputStream.copyTo(pipedOutputStream)
                }
            }
        }.start()

        return pipedInputStream
    }

    fun webResourceResponse(request: WebResourceRequest?): WebResourceResponse? {

        if (request!!.url == null || URLUtil.isHttpsUrl(request!!.url.toString())){
            return null
        }

        if (isMediaFile(request.url.toString())){
            val connection = URL(request.url.toString()).openConnection()
            val input = pipedInputStream(connection.getInputStream(), connection.contentLength)

            return WebResourceResponse("", "", input)
        }

        return null
    }
}