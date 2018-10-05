package com.lgo.library.XWalk

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.support.annotation.UiThread
import android.util.Log
import android.webkit.URLUtil
import android.widget.Toast
import com.lgo.library.Shared
import com.lgo.library.Storage
import java.io.*

class Cache {

    private val TAG = "CACHE"
    private val mediaFolder = "media"
    private val tempFolder = "temp"
    lateinit var storage: Storage
    private lateinit var context: Context
    private var caching = mutableMapOf<String, String>()
    private lateinit var mediaFiles: ArrayList<String>

    constructor(context: Context){
        this.context = context
        this.storage = Storage(context, Storage.Location.External)
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

    fun buildFileFromUrl(uri: Uri): File {
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

    fun buildTempFilePath(uri: Uri): File {
        return File(tempFolder, buildFileFromUrl(uri).toString())
    }

    fun checkFileExists(file: File): Boolean {
        return storage.checkFileExists(file)
    }

    fun readFile(file: File): FileInputStream?{
        return storage.readFileInputStream(file)
    }

    fun cacheOnInputStream(inputStream: InputStream, file: File, request: String): InputStream {
        val pipedInputStream = PipedInputStream()
        val pipedOutputStream = PipedOutputStream(pipedInputStream)
        val tempFile = File(tempFolder, file.toString())

        Thread() {
            Log.i(TAG, "Thread ${Thread.currentThread().id} Cache $request Start")
            val fileOutputStream = storage.writeFileOutputStream(tempFile)
            while (true){
                try {
                    val buffer = ByteArray(1024)
                    val length = inputStream.read(buffer, 0, buffer.size)
                    if (length < 0){
                        break
                    }

                    fileOutputStream?.write(buffer, 0, length)
                    pipedOutputStream.write(buffer, 0, length)

                }catch (ex: Exception){
                    Log.i(TAG, "Exception" + ex.toString())
                }
            }
            storage.move(tempFile, file)


            while (true){

                try {
                    val available = pipedInputStream.available()
                    if (available <= 0){
                        break
                    }
                }catch (ex: Exception){
                    break
                }

                Thread.sleep(100)
            }

            pipedOutputStream.flush()
            pipedOutputStream.close()

            Log.i(TAG, "Thread ${Thread.currentThread().id} Cache Stop")

            val handler = Handler(Looper.getMainLooper())
            handler.post {
                Toast.makeText(context, "Request $request cache done", Toast.LENGTH_LONG).show()
            }
        }.start()

        return pipedInputStream
    }
}