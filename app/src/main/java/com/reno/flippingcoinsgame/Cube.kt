package com.reno.flippingcoinsgame

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer


class Cube {
    private val colorStride: Int = 4 * 4
    private val vertexShaderCode =
    // This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
        "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "attribute vec4 vColor;" +
                "varying vec4 _vColor;" +
                "void main() {" +
                "  _vColor = vColor;" +
                "  gl_Position = uMVPMatrix * vPosition;" +
                "}";
    private val fragmentShaderCode =
        "precision mediump float;" +
                "varying vec4 _vColor;" +
                "void main() {" +
                "  gl_FragColor = _vColor;" +
                "}";

    private val vertexBuffer: FloatBuffer
    private val indexBuffer: IntBuffer
    private val colorBuffer: FloatBuffer

    private val mProgram: Int
    private var mPositionHandle = 0
    private var mColorHandle = 0
    private var mMVPMatrixHandle = 0
    private val drawOrder = intArrayOf(
        0, 1, 3, 3, 1, 2, // Front face.
        0, 1, 4, 4, 5, 1, // Bottom face.
        1, 2, 5, 5, 6, 2, // Right face.
        2, 3, 6, 6, 7, 3, // Top face.
        3, 7, 4, 4, 3, 0, // Left face.
        4, 5, 7, 7, 6, 5 // Rear face.
    ) // order to draw vertices
    private val vertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
    var color = floatArrayOf(
        0.0f, 1.0f, 1.0f, 1.0f,
        1.0f, 0.0f, 0.0f, 1.0f,
        1.0f, 1.0f, 0.0f, 1.0f,
        0.0f, 1.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f, 1.0f,
        1.0f, 0.0f, 1.0f, 1.0f,
        1.0f, 1.0f, 1.0f, 1.0f,
        0.0f, 1.0f, 1.0f, 1.0f
    )

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    fun draw(mvpMatrix: FloatArray?) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram)
        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
            mPositionHandle, COORDS_PER_VERTEX,
            GLES20.GL_FLOAT, false,
            vertexStride, vertexBuffer
        )
        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "vColor")
        GLES20.glEnableVertexAttribArray(mColorHandle)
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, colorStride, colorBuffer)

        // Set color for drawing the triangle
//        GLES20.glUniform4fv(mColorHandle, 1, color, 0)
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
        MyGLRenderer.checkGlError("glGetUniformLocation")
        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)
        MyGLRenderer.checkGlError("glUniformMatrix4fv")
        // Draw the square
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES, drawOrder.size,
            GLES20.GL_UNSIGNED_INT, indexBuffer
        )
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    companion object {
        // number of coordinates per vertex in this array
        const val COORDS_PER_VERTEX = 3
        var squareCoords = floatArrayOf(
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f
        )
    }

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    init {
        // initialize vertex byte buffer for shape coordinates
        val bb: ByteBuffer =
            ByteBuffer.allocateDirect( // (# of coordinate values * 4 bytes per float)
                squareCoords.size * 4
            )
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(squareCoords)
        vertexBuffer.position(0)
        // initialize byte buffer for the draw list
        // # of coordinate values * 4 bytes per int
        val dlb: ByteBuffer = ByteBuffer.allocateDirect(drawOrder.size * 4)
        dlb.order(ByteOrder.nativeOrder())
        indexBuffer = dlb.asIntBuffer()
        indexBuffer.put(drawOrder)
        indexBuffer.position(0)
//        drawListBuffer = dlb.asShortBuffer()
//        drawListBuffer.put(drawOrder)
//        drawListBuffer.position(0)

        val byteBuffer =
            ByteBuffer.allocateDirect(color.size * 4)
        byteBuffer.order(ByteOrder.nativeOrder())
        colorBuffer = byteBuffer.asFloatBuffer()
        colorBuffer.put(color)
        colorBuffer.position(0)

        // prepare shaders and OpenGL program
        val vertexShader = MyGLRenderer.loadShader(
            GLES20.GL_VERTEX_SHADER,
            vertexShaderCode
        )
        val fragmentShader = MyGLRenderer.loadShader(
            GLES20.GL_FRAGMENT_SHADER,
            fragmentShaderCode
        )
        mProgram = GLES20.glCreateProgram() // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader) // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader) // add the fragment shader to program
        GLES20.glLinkProgram(mProgram) // create OpenGL program executables
    }
}