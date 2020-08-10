package com.reno.flippingcoinsgame

import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * https://android.googlesource.com/platform/development/+/master/samples/OpenGL?autodive=0
 * */

class MainActivity : AppCompatActivity() {
    private var mGLView: GLSurfaceView? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity
        mGLView = MyGLSurfaceView(this)
        setContentView(mGLView)
    }

    override fun onPause() {
        super.onPause()
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        mGLView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        mGLView?.onResume()
    }
}