package com.lgo.library

import android.provider.SyncStateContract.Helpers.update
import java.security.NoSuchAlgorithmException


class Shared {

    constructor(){

    }

    companion object {

        fun MD5(s: String): String {
            try {
                // Create MD5 Hash
                val digest = java.security.MessageDigest.getInstance("MD5")
                digest.update(s.toByteArray())
                val messageDigest = digest.digest()

                // Create Hex String
                val hexString = StringBuffer()
                for (i in messageDigest.indices)
                    hexString.append(Integer.toHexString(0xFF and messageDigest[i].toInt()))

                return hexString.toString()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }

            return ""
        }
    }


}