package com.lgo.library

import android.webkit.WebResourceResponse
import java.io.*
import java.lang.Exception

class FakeInputStream: InputStream {

    var inputStream: InputStream? = null
    var contentLength: Int = 0

    constructor(inputStream: InputStream, length: Int){
        this.inputStream = inputStream
        this.contentLength = length
    }

    override fun read(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return inputStream!!.read()
    }

    override fun read(b: ByteArray?): Int {
        return inputStream!!.read(b)
    }

    override fun read(b: ByteArray?, off: Int, len: Int): Int {
        return inputStream!!.read(b, off, len)
    }

    override fun reset() {
        inputStream!!.reset()
    }

    override fun mark(readlimit: Int) {
        inputStream!!.mark(readlimit)
    }

    override fun available(): Int {
        return inputStream!!.available()
    }

    override fun markSupported(): Boolean {
        return inputStream!!.markSupported()
    }

    override fun equals(other: Any?): Boolean {
        return inputStream!!.equals(other)
    }

    override fun hashCode(): Int {
        return inputStream!!.hashCode()
    }

    override fun skip(n: Long): Long {
        return inputStream!!.skip(n)
    }

    override fun toString(): String {
        return inputStream!!.toString()
    }

    override fun close() {
        inputStream!!.close()
    }

}
