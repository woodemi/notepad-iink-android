package com.myscript.notepad_iink

import android.app.Application
import android.content.Context
import android.graphics.Typeface
import com.myscript.iink.Engine
import com.myscript.iink.uireferenceimplementation.FontUtils
import java.io.File
import java.io.IOException

class App : Application() {
    companion object {
        lateinit var engine: Engine
        val typefaceMap = HashMap<String, Typeface>()
    }

    override fun onCreate() {
        super.onCreate()
        engine = Engine.create(MyCertificate.getBytes())
        engine.initPath(this)
        loadFonts()
    }

    private fun loadFonts() {
        val assets = assets
        try {
            val assetsDir = "fonts"
            val files = assets.list(assetsDir)!!
            for (filename in files) {
                val fontPath = assetsDir + File.separatorChar + filename
                val fontFamily = FontUtils.getFontFamily(assets, fontPath)
                val typeface = Typeface.createFromAsset(assets, fontPath)
                if (fontFamily != null && typeface != null) {
                    typefaceMap[fontFamily] = typeface
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

fun Engine.initPath(context: Context) {
    // configure recognition
    val conf = configuration
    val confDir = "zip://${context.packageCodePath}!/assets/conf"
    conf.setStringArray("configuration-manager.search-path", arrayOf(confDir))
    val tempDir = context.filesDir.path + File.separator + "tmp"
    conf.setString("content-package.temp-folder", tempDir)
}