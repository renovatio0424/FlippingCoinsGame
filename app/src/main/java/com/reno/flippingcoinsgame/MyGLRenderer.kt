package com.reno.flippingcoinsgame

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private var square: Square? = null

    override fun onSurfaceCreated(gl10: GL10, eglConfig: EGLConfig) {
        square = Square(context)
    }

    override fun onSurfaceChanged(gl10: GL10, w: Int, h: Int) {
        GLES20.glViewport(0, 0, w, h)
    }

    override fun onDrawFrame(gl10: GL10) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        square?.draw()
    }

}
