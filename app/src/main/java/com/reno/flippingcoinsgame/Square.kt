package com.reno.flippingcoinsgame

import android.content.Context
import android.opengl.GLES20
import com.reno.flippingcoinsgame.util.BufferUtils.newFloatBuffer
import com.reno.flippingcoinsgame.util.BufferUtils.newShortBuffer
import com.reno.flippingcoinsgame.util.ShaderProgram
import com.reno.flippingcoinsgame.util.ShaderUtils
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer


/**
 * code from the url at { @link https://developer.android.com/training/graphics/opengl/shapes.html }
 * Created by burt on 2016. 6. 16..
 */
class Square(context: Context) {
    private var shader: ShaderProgram? = null
    private var vertexBuffer: FloatBuffer? = null
    private var vertexBufferId = 0
    private var vertexCount = 0
    private var vertexStride = 0
    private var indexBuffer: ShortBuffer? = null
    private var indexBufferId = 0

    private fun setupShader(context: Context) {
        // compile & link shader
        shader = ShaderProgram(
            ShaderUtils.readShaderFile(context, R.raw.simple_vertex_shader),
            ShaderUtils.readShaderFile(context, R.raw.simple_fragment_shader)
        )
    }

    private fun setupVertexBuffer() {
        // initialize vertex float buffer for shape coordinates
        vertexBuffer = newFloatBuffer(squareCoords.size)

        // add the coordinates to the FloatBuffer
        vertexBuffer!!.put(squareCoords)

        // set the buffer to read the first coordinate
        vertexBuffer!!.position(0)


        //copy vertices from cpu to the gpu
        val buffer = IntBuffer.allocate(1)
        GLES20.glGenBuffers(1, buffer)
        vertexBufferId = buffer[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId)
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            squareCoords.size * SIZE_OF_FLOAT,
            vertexBuffer,
            GLES20.GL_STATIC_DRAW
        )
        vertexCount =
            squareCoords.size / (COORDS_PER_VERTEX + COLORS_PER_VERTEX)
        vertexStride =
            (COORDS_PER_VERTEX + COLORS_PER_VERTEX) * SIZE_OF_FLOAT // 4 bytes per vertex
    }

    private fun setupIndexBuffer() {
        // initialize index short buffer for index
        indexBuffer = newShortBuffer(indexes.size)
        indexBuffer!!.put(indexes)
        indexBuffer!!.position(0)
        val buffer = IntBuffer.allocate(1)
        GLES20.glGenBuffers(1, buffer)
        indexBufferId = buffer[0]
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBufferId)
        GLES20.glBufferData(
            GLES20.GL_ELEMENT_ARRAY_BUFFER,
            indexes.size * SIZE_OF_SHORT,
            indexBuffer,
            GLES20.GL_STATIC_DRAW
        )
    }

    fun draw() {
        shader!!.begin()
        shader!!.enableVertexAttribute("a_Position")
        shader!!.setVertexAttribute(
            "a_Position",
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            0
        )
        shader!!.enableVertexAttribute("a_Color")
        shader!!.setVertexAttribute(
            "a_Color",
            COLORS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            COORDS_PER_VERTEX * SIZE_OF_FLOAT
        )
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBufferId)
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,  // mode
            indexes.size,  // count
            GLES20.GL_UNSIGNED_SHORT,  // type
            0
        ) // offset
        shader!!.disableVertexAttribute("a_Position")
        shader!!.disableVertexAttribute("a_Color")
        shader!!.end()
    }

    companion object {
        // number of coordinates per vertex in this array
        const val COORDS_PER_VERTEX = 3
        const val COLORS_PER_VERTEX = 4
        const val SIZE_OF_FLOAT = 4
        const val SIZE_OF_SHORT = 2
        val squareCoords = floatArrayOf(
            -0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,  // top left
            -0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f,  // bottom left
            0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f,  // bottom right
            0.5f, 0.5f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f // top right
        )
        val indexes = shortArrayOf(
            0, 1, 2,
            0, 2, 3
        )
    }

    init {
        setupShader(context)
        setupVertexBuffer()
        setupIndexBuffer()
    }
}