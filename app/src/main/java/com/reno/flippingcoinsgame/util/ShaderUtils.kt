package com.reno.flippingcoinsgame.util

import android.content.Context
import android.content.res.AssetManager
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*


object ShaderUtils {
    fun readShaderFile(context: Context, resourceId: Int): String {
        val inputStream: InputStream = context.resources.openRawResource(resourceId)
        val inputStreamReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)
        var nextLine: String?
        val body = StringBuilder()
        try {
            while (bufferedReader.readLine().also { nextLine = it } != null) {
                body.append(nextLine)
                body.append("\n")
            }
        } catch (e: IOException) {
            return ""
        }
        val code = body.toString()
        return code//body.toString()
    }

    fun readShaderFileFromFilePath(context: Context, filePath: String?): String? {
        try {
            val assetManager: AssetManager = context.assets
            val ims: InputStream = assetManager.open(filePath!!)
            val re = convertStreamToString(ims)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun convertStreamToString(`is`: InputStream?): String {
        val s: Scanner = Scanner(`is`).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }
}