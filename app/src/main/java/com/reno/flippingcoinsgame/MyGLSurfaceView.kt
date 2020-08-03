package com.reno.flippingcoinsgame

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class MyGLSurfaceView : GLSurfaceView {
    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        // use opengl es 2.0
        setEGLContextClientVersion(2)

        // store opengl context
        preserveEGLContextOnPause = true

        // set renderer
        setRenderer(MyGLRenderer(context))
    }
}