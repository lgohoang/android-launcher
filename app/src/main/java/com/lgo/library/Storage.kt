package com.lgo.library

import android.content.Context
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import android.webkit.URLUtil
import android.widget.Toast
import java.io.*
import java.net.URL
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.Comparator

class Storage {

    enum class Location {
        Internal, External
    }

    private val TAG = "STORAGE"
    private val cacheDir = "cache"

    private var location: Location = Location.Internal
    private var dir: File? = null
    private var context: Context? = null

    constructor(context: Context, location: Location){
        this.context = context
        this.location = location

        init()
    }

    private fun init() {
        when(location){

            Location.Internal -> {
                dir = File(context?.filesDir, cacheDir)
            }

            Location.External -> {
                if (checkExternalMounted()){
                    dir = File(Environment.getExternalStorageDirectory(), cacheDir)
                }else{
                    Toast.makeText(context, "External storage unmounted", Toast.LENGTH_LONG).show()
                    dir = File(context?.filesDir, cacheDir)
                }
            }
        }

        if (!dir!!.exists()){
            Log.i(TAG, "Mkdir: " + dir + " : " + dir?.mkdirs())
        }

        Log.i(TAG, "Cache Dir: " + dir)

        //Handle storage free

//        Thread{
//            while (true){
//                Log.i(TAG, "Clean up thread running")
//
//                if (checkFreeSizeStorage() > 100){
//                    Log.i(TAG, "Storage > 70%")
//
//                }else{
//                    Log.i(TAG, "Storage < 70%")
//                    cleanUpStorage(File(dir, "media"))
//                }
//
//                Thread.sleep(5000)
//            }
//        }.start()
    }


    fun spilitPath(file: File):Pair<File, File?>{
        var fn = URLUtil.guessFileName(file.toString(), null, null)
        var path = file.toString().replace(fn, "")

        return Pair(File(path), File(fn))
    }

    fun writeFileOutputStream(file: File): FileOutputStream? {
        val f = File(this.dir, file.toString())
        val (path, fn) = spilitPath(f)

        if (!path.exists()){
            val t = path.mkdirs()
            Log.i(TAG, "Mkdir: " + path + " : " + t)
        }

        if (fn != null){
            Log.i(TAG, "writeFile: " + f)
            return FileOutputStream(f)
        }

        return null
    }


    fun download(link: String, file: File){

        doAsync{
            val file = File(this.dir, file.toString())
            val input = URL(link).openStream()
            val output = FileOutputStream(file)

            input.use { _ ->
                output.use { _ ->
                    input.copyTo(output)
                }
            }

        }
    }

    fun move(file: File, dest: File){
        val f = File(this.dir, file.toString())
        val d = File(this.dir, dest.toString())
        val (path, fn) = spilitPath(d)

        if (!path.exists()){
            val t = path.mkdirs()
        }

        f.renameTo(d)
    }

    fun readFileInputStream(file: File): InputStream? {
        val f = File(this.dir, file.toString())



        if (f.exists()){
            f.setLastModified(Calendar.getInstance().timeInMillis)
            return  f.inputStream()
        }

        return null
    }

    fun checkFileExists(file: File): Boolean {
        val f = File(this.dir, file.toString())
        return f.exists()
    }

    fun cleanUpStorage(file: File){
        Log.i(TAG, "Clean up dir: " + file)
        //clean dir
        var listFile = file?.walk()?.toList().filter {
            it.isFile
        }

        Collections.sort(listFile, Comparator{o1: File, o2: File ->
            if ((o1 as File).lastModified() > (o2 as File).lastModified()) {
                +1
            } else if ((o1 as File).lastModified() < (o2 as File).lastModified()) {
                -1
            } else {
                0
            }
        })

        if(!listFile!!.isEmpty()){
            val delFile = listFile?.first()

            delFile?.delete()

            Log.i(TAG, "Delete dir: " + delFile)

        }
    }

    fun checkFreeSizeStorage(): Int {
        return ((dir!!.freeSpace.toFloat() / dir!!.totalSpace.toFloat()) * 100).toInt()
    }

    private fun checkExternalMounted(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

}