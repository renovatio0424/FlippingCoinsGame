package com.reno.flippingcoinsgame.util

import android.graphics.Color
import android.graphics.PointF
import android.opengl.GLES20
import android.renderscript.Matrix3f
import android.renderscript.Matrix4f
import java.nio.Buffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.util.*


/**
 * A shader program encapsulates a vertex and fragment shader pair linked to form a shader program useable with OpenGL ES 2.0.
 * {@see https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/graphics/glutils/ShaderProgram.java }
 */
class ShaderProgram(vertexShader: String?, fragmentShader: String?) {
    private val vertexShaderSource: String
    private val fragmentShaderSource: String
    private var vertexShaderHandle = 0
    private var fragmentShaderHandle = 0
    private var programHandle = 0
    private var log = ""

    // is this valid shader program?
    private var isValid = false

    private val uniforms = HashMap<String, Int>()
    private val attributes = HashMap<String, Int>()

    // start to use shader program
    fun begin() {
        GLES20.glUseProgram(programHandle)
    }

    // end of using shader program
    fun end() {
        GLES20.glUseProgram(0)
    }

    // destory the shader program
    fun destroy() {
        GLES20.glUseProgram(0)
        GLES20.glDeleteShader(vertexShaderHandle)
        GLES20.glDeleteShader(fragmentShaderHandle)
        GLES20.glDeleteProgram(programHandle)
    }

    private fun compileShaders(vertexShader: String, fragmentShader: String) {
        vertexShaderHandle = loadShader(GLES20.GL_VERTEX_SHADER, vertexShader)
        fragmentShaderHandle = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)
        if (vertexShaderHandle == -1 || fragmentShaderHandle == -1) {
            isValid = false
            return
        }
        programHandle = linkProgram(createProgram())
        if (programHandle == -1) {
            isValid = false
            return
        }
        isValid = true
    }

    private fun createProgram(): Int {
        val program = GLES20.glCreateProgram()
        return if (program != 0) program else -1
    }

    private fun linkProgram(program: Int): Int {
        if (program == -1) return -1
        GLES20.glAttachShader(program, vertexShaderHandle)
        GLES20.glAttachShader(program, fragmentShaderHandle)
        GLES20.glLinkProgram(program)
        val linked = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linked, 0)
        if (linked[0] == GLES20.GL_FALSE) {
            val infoLog = GLES20.glGetProgramInfoLog(program)
            log += infoLog
            GLES20.glDeleteProgram(program)
            return -1
        }
        return program
    }

    private fun loadShader(shaderType: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(shaderType)
        if (shader == 0) return -1
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == GLES20.GL_FALSE) {
            val infoLog = GLES20.glGetShaderInfoLog(shader)
            log += infoLog
            GLES20.glDeleteShader(shader)
            return -1
        }
        return shader
    }

    fun getLog(): String {
        return if (isValid) {
            log = GLES20.glGetProgramInfoLog(programHandle)
            log
        } else {
            log
        }
    }

    private fun fetchAttributeLocation(name: String): Int {
        var location = attributes[name]!!
        if (location == -1) {
            location = GLES20.glGetAttribLocation(programHandle, name)
            if (location != -1) {
                attributes[name] = location
            }
        }
        return location
    }

    private fun fetchUniformLocation(name: String): Int {
        var location = uniforms[name]!!
        if (location == -1) {
            location = GLES20.glGetUniformLocation(programHandle, name)
            if (location != -1) {
                uniforms[name] = location
            }
        }
        return location
    }

    private fun fetchAttributes() {
        attributes.clear()
        val params = IntBuffer.allocate(1)
        val type = IntBuffer.allocate(1)
        GLES20.glGetProgramiv(programHandle, GLES20.GL_ACTIVE_ATTRIBUTES, params)
        val numAttributes = params[0]
        for (i in 0 until numAttributes) {
            params.compact()
            params.put(0, 1)
            type.clear()
            val name = GLES20.glGetActiveAttrib(programHandle, i, params, type)
            val location = GLES20.glGetAttribLocation(programHandle, name)
            attributes[name] = location
        }
    }

    private fun fetchUniforms() {
        uniforms.clear()
        val params = IntBuffer.allocate(1)
        val type = IntBuffer.allocate(1)
        GLES20.glGetProgramiv(programHandle, GLES20.GL_ACTIVE_UNIFORMS, params)
        val numUniform = params[0]
        for (i in 0 until numUniform) {
            params.compact()
            params.put(0, 1)
            type.clear()
            val name = GLES20.glGetActiveUniform(programHandle, i, params, type)
            val location = GLES20.glGetUniformLocation(programHandle, name)
            uniforms[name] = location
        }
    }

    /**
     * @param name name the name of the uniform
     * @return the location of the uniform or -1.
     */
    fun getUniformLocation(name: String?): Int? {
        return if (uniforms.containsKey(name)) -1 else uniforms[name]
    }

    /**
     * @param name the name of the attribute
     * @return the location of the attribute or -1.
     */
    fun getAttributeLocation(name: String?): Int? {
        return if (attributes[name] == null) -1 else attributes[name]
    }

    /** @param name the name of the uniform
     * @return whether the uniform is available in the shader
     */
    fun hasUniform(name: String?): Boolean {
        return uniforms.containsKey(name)
    }

    /** @param name the name of the attribute
     * @return whether the attribute is available in the shader
     */
    fun hasAttribute(name: String?): Boolean {
        return attributes.containsKey(name)
    }
    /***********************************************************************************************
     * Set Uniforms and Attributes
     */
    /** Sets the uniform with the given name.
     *
     * @param name the name of the uniform
     * @param value the value
     */
    fun setUniformi(name: String, value: Int) {
        val location = fetchUniformLocation(name)
        if (location == -1) return
        GLES20.glUniform1i(location, value)
    }

    fun setUniformi(location: Int, value: Int) {
        if (location == -1) return
        GLES20.glUniform1i(location, value)
    }

    /** Sets the uniform with the given name.
     *
     * @param name the name of the uniform
     * @param value1 the first value
     * @param value2 the second value
     */
    fun setUniformi(name: String, value1: Int, value2: Int) {
        val location = fetchUniformLocation(name)
        if (location == -1) return
        GLES20.glUniform2i(location, value1, value2)
    }

    fun setUniformi(location: Int, value1: Int, value2: Int) {
        if (location == -1) return
        GLES20.glUniform2i(location, value1, value2)
    }

    /** Sets the uniform with the given name.
     *
     * @param name the name of the uniform
     * @param value1 the first value
     * @param value2 the second value
     * @param value3 the third value
     */
    fun setUniformi(name: String, value1: Int, value2: Int, value3: Int) {
        val location = fetchUniformLocation(name)
        if (location == -1) return
        GLES20.glUniform3i(location, value1, value2, value3)
    }

    fun setUniformi(location: Int, value1: Int, value2: Int, value3: Int) {
        if (location != -1) GLES20.glUniform3i(location, value1, value2, value3)
    }

    /** Sets the uniform with the given name.
     *
     * @param name the name of the uniform
     * @param value1 the first value
     * @param value2 the second value
     * @param value3 the third value
     * @param value4 the fourth value
     */
    fun setUniformi(name: String, value1: Int, value2: Int, value3: Int, value4: Int) {
        val location = fetchUniformLocation(name)
        if (location == -1) return
        GLES20.glUniform4i(location, value1, value2, value3, value4)
    }

    fun setUniformi(location: Int, value1: Int, value2: Int, value3: Int, value4: Int) {
        if (location == -1) return
        GLES20.glUniform4i(location, value1, value2, value3, value4)
    }

    /** Sets the uniform with the given name.
     *
     * @param name the name of the uniform
     * @param value the value
     */
    fun setUniformf(name: String, value: Float) {
        val location = fetchUniformLocation(name)
        if (location == -1) return
        GLES20.glUniform1f(location, value)
    }

    fun setUniformf(location: Int, value: Float) {
        if (location == -1) return
        GLES20.glUniform1f(location, value)
    }

    /** Sets the uniform with the given name.
     *
     * @param name the name of the uniform
     * @param value1 the first value
     * @param value2 the second value
     */
    fun setUniformf(name: String, value1: Float, value2: Float) {
        val location = fetchUniformLocation(name)
        if (location == -1) return
        GLES20.glUniform2f(location, value1, value2)
    }

    fun setUniformf(location: Int, value1: Float, value2: Float) {
        if (location == -1) return
        GLES20.glUniform2f(location, value1, value2)
    }

    /** Sets the uniform with the given name.
     *
     * @param name the name of the uniform
     * @param value1 the first value
     * @param value2 the second value
     * @param value3 the third value
     */
    fun setUniformf(
        name: String,
        value1: Float,
        value2: Float,
        value3: Float
    ) {
        val location = fetchUniformLocation(name)
        if (location == -1) return
        GLES20.glUniform3f(location, value1, value2, value3)
    }

    fun setUniformf(location: Int, value1: Float, value2: Float, value3: Float) {
        if (location == -1) return
        GLES20.glUniform3f(location, value1, value2, value3)
    }

    /** Sets the uniform with the given name.
     *
     * @param name the name of the uniform
     * @param value1 the first value
     * @param value2 the second value
     * @param value3 the third value
     * @param value4 the fourth value
     */
    fun setUniformf(
        name: String,
        value1: Float,
        value2: Float,
        value3: Float,
        value4: Float
    ) {
        val location = fetchUniformLocation(name)
        if (location == -1) return
        GLES20.glUniform4f(location, value1, value2, value3, value4)
    }

    fun setUniformf(
        location: Int,
        value1: Float,
        value2: Float,
        value3: Float,
        value4: Float
    ) {
        if (location == -1) return
        GLES20.glUniform4f(location, value1, value2, value3, value4)
    }

    fun setUniform1fv(name: String, values: FloatArray?, offset: Int, length: Int) {
        val location = fetchUniformLocation(name)
        if (location == -1) return
        GLES20.glUniform1fv(location, length, values, offset)
    }

    fun setUniform1fv(location: Int, values: FloatArray?, offset: Int, length: Int) {
        if (location == -1) return
        GLES20.glUniform1fv(location, length, values, offset)
    }

    fun setUniform2fv(name: String, values: FloatArray?, offset: Int, length: Int) {
        val location = fetchUniformLocation(name)
        if (location == -1) return
        GLES20.glUniform2fv(location, length / 2, values, offset)
    }

    fun setUniform2fv(location: Int, values: FloatArray?, offset: Int, length: Int) {
        if (location == -1) return
        GLES20.glUniform2fv(location, length / 2, values, offset)
    }

    fun setUniform3fv(name: String, values: FloatArray?, offset: Int, length: Int) {
        val location = fetchUniformLocation(name)
        if (location == -1) return
        GLES20.glUniform3fv(location, length / 3, values, offset)
    }

    fun setUniform3fv(location: Int, values: FloatArray?, offset: Int, length: Int) {
        if (location == -1) return
        GLES20.glUniform3fv(location, length / 3, values, offset)
    }

    fun setUniform4fv(name: String, values: FloatArray?, offset: Int, length: Int) {
        val location = fetchUniformLocation(name)
        if (location == -1) return
        GLES20.glUniform4fv(location, length / 4, values, offset)
    }

    fun setUniform4fv(location: Int, values: FloatArray?, offset: Int, length: Int) {
        if (location == -1) return
        GLES20.glUniform4fv(location, length / 4, values, offset)
    }

    /** Sets the uniform matrix with the given name.
     *
     * @param name the name of the uniform
     * @param matrix the matrix
     */
    fun setUniformMatrix(name: String, matrix: Matrix4f) {
        setUniformMatrix(name, matrix, false)
    }

    /** Sets the uniform matrix with the given name.
     *
     * @param name the name of the uniform
     * @param matrix the matrix
     * @param transpose whether the matrix should be transposed
     */
    fun setUniformMatrix(name: String, matrix: Matrix4f, transpose: Boolean) {
        setUniformMatrix(fetchUniformLocation(name), matrix, transpose)
    }

    fun setUniformMatrix(location: Int, matrix: Matrix4f) {
        setUniformMatrix(location, matrix, false)
    }

    fun setUniformMatrix(location: Int, matrix: Matrix4f, transpose: Boolean) {
        if (location == -1) return
        GLES20.glUniformMatrix4fv(location, 1, transpose, matrix.array, 0)
    }

    /** Sets the uniform matrix with the given name.
     *
     * @param name the name of the uniform
     * @param matrix the matrix
     */
    fun setUniformMatrix(name: String, matrix: Matrix3f) {
        setUniformMatrix(name, matrix, false)
    }

    /** Sets the uniform matrix with the given name.
     *
     * @param name the name of the uniform
     * @param matrix the matrix
     * @param transpose whether the uniform matrix should be transposed
     */
    fun setUniformMatrix(name: String, matrix: Matrix3f, transpose: Boolean) {
        setUniformMatrix(fetchUniformLocation(name), matrix, transpose)
    }

    fun setUniformMatrix(location: Int, matrix: Matrix3f) {
        setUniformMatrix(location, matrix, false)
    }

    fun setUniformMatrix(location: Int, matrix: Matrix3f, transpose: Boolean) {
        if (location == -1) return
        GLES20.glUniformMatrix3fv(location, 1, transpose, matrix.array, 0)
    }

    /** Sets an array of uniform matrices with the given name.
     *
     * @param name the name of the uniform
     * @param buffer buffer containing the matrix data
     * @param transpose whether the uniform matrix should be transposed
     */
    fun setUniformMatrix3fv(
        name: String,
        buffer: FloatBuffer,
        count: Int,
        transpose: Boolean
    ) {
        buffer.position(0)
        val location = fetchUniformLocation(name)
        if (location == -1) return
        GLES20.glUniformMatrix3fv(location, count, transpose, buffer)
    }

    /** Sets an array of uniform matrices with the given name.
     *
     * @param name the name of the uniform
     * @param buffer buffer containing the matrix data
     * @param transpose whether the uniform matrix should be transposed
     */
    fun setUniformMatrix4fv(
        name: String,
        buffer: FloatBuffer,
        count: Int,
        transpose: Boolean
    ) {
        buffer.position(0)
        val location = fetchUniformLocation(name)
        if (location == -1) return
        GLES20.glUniformMatrix4fv(location, count, transpose, buffer)
    }

    fun setUniformMatrix4fv(location: Int, values: FloatArray?, offset: Int, length: Int) {
        if (location == -1) return
        GLES20.glUniformMatrix4fv(location, length / 16, false, values, offset)
    }

    fun setUniformMatrix4fv(name: String, values: FloatArray?, offset: Int, length: Int) {
        setUniformMatrix4fv(fetchUniformLocation(name), values, offset, length)
    }

    /** Sets the uniform with the given name.
     *
     * @param name the name of the uniform
     * @param vector x and y as the first and second values respectively
     */
    fun setUniformf(name: String, vector: PointF) {
        setUniformf(name, vector.x, vector.y)
    }

    fun setUniformf(location: Int, vector: PointF) {
        setUniformf(location, vector.x, vector.y)
    }

    /** Sets the uniform with the given name.
     *
     * @param name the name of the uniform
     * @param color r, g, b and a as the first through fourth values respectively
     */
    fun setUniformColor(name: String, color: Int) {
        setUniformf(
            name,
            Color.red(color) / 255.0f,
            Color.green(color) / 255.0f,
            Color.blue(color) / 255.0f,
            Color.alpha(color) / 255.0f
        )
    }

    fun setUniformf(location: Int, color: Int) {
        setUniformf(
            location,
            Color.red(color) / 255.0f,
            Color.green(color) / 255.0f,
            Color.blue(color) / 255.0f,
            Color.alpha(color) / 255.0f
        )
    }

    /** Sets the vertex attribute with the given name.
     *
     * @param name the attribute name
     * @param size the number of components, must be >= 1 and <= 4
     * @param type the type, must be one of GL20.GL_BYTE, GL20.GL_UNSIGNED_BYTE, GL20.GL_SHORT,
     * GL20.GL_UNSIGNED_SHORT,GL20.GL_FIXED, or GL20.GL_FLOAT. GL_FIXED will not work on the desktop
     * @param normalize whether fixed point data should be normalized. Will not work on the desktop
     * @param stride the stride in bytes between successive attributes
     * @param buffer the buffer containing the vertex attributes.
     */
    fun setVertexAttribute(
        name: String,
        size: Int,
        type: Int,
        normalize: Boolean,
        stride: Int,
        buffer: Buffer?
    ) {
        val location = fetchAttributeLocation(name)
        if (location == -1) return
        GLES20.glVertexAttribPointer(location, size, type, normalize, stride, buffer)
    }

    fun setVertexAttribute(
        location: Int,
        size: Int,
        type: Int,
        normalize: Boolean,
        stride: Int,
        buffer: Buffer?
    ) {
        if (location == -1) return
        GLES20.glVertexAttribPointer(location, size, type, normalize, stride, buffer)
    }

    /** Sets the vertex attribute with the given name.
     *
     * @param name the attribute name
     * @param size the number of components, must be >= 1 and <= 4
     * @param type the type, must be one of GL20.GL_BYTE, GL20.GL_UNSIGNED_BYTE, GL20.GL_SHORT,
     * GL20.GL_UNSIGNED_SHORT,GL20.GL_FIXED, or GL20.GL_FLOAT. GL_FIXED will not work on the desktop
     * @param normalize whether fixed point data should be normalized. Will not work on the desktop
     * @param stride the stride in bytes between successive attributes
     * @param offset byte offset into the vertex buffer object bound to GL20.GL_ARRAY_BUFFER.
     */
    fun setVertexAttribute(
        name: String,
        size: Int,
        type: Int,
        normalize: Boolean,
        stride: Int,
        offset: Int
    ) {
        val location = fetchAttributeLocation(name)
        if (location == -1) return
        GLES20.glVertexAttribPointer(location, size, type, normalize, stride, offset)
    }

    fun setVertexAttribute(
        location: Int,
        size: Int,
        type: Int,
        normalize: Boolean,
        stride: Int,
        offset: Int
    ) {
        if (location == -1) return
        GLES20.glVertexAttribPointer(location, size, type, normalize, stride, offset)
    }

    /** Disables the vertex attribute with the given name
     *
     * @param name the vertex attribute name
     */
    fun disableVertexAttribute(name: String) {
        val location = fetchAttributeLocation(name)
        if (location == -1) return
        GLES20.glDisableVertexAttribArray(location)
    }

    fun disableVertexAttribute(location: Int) {
        if (location == -1) return
        GLES20.glDisableVertexAttribArray(location)
    }

    /** Enables the vertex attribute with the given name
     *
     * @param name the vertex attribute name
     */
    fun enableVertexAttribute(name: String) {
        val location = fetchAttributeLocation(name)
        if (location == -1) return
        GLES20.glEnableVertexAttribArray(location)
    }

    fun enableVertexAttribute(location: Int) {
        if (location == -1) return
        GLES20.glEnableVertexAttribArray(location)
    }

    /** Sets the given attribute
     *
     * @param name the name of the attribute
     * @param value1 the first value
     * @param value2 the second value
     * @param value3 the third value
     * @param value4 the fourth value
     */
    fun setAttributef(
        name: String,
        value1: Float,
        value2: Float,
        value3: Float,
        value4: Float
    ) {
        val location = fetchAttributeLocation(name)
        if (location == -1) return
        GLES20.glVertexAttrib4f(location, value1, value2, value3, value4)
    }

    fun setAttributef(
        name: String,
        value1: Float,
        value2: Float,
        value3: Float
    ) {
        val location = fetchAttributeLocation(name)
        if (location == -1) return
        GLES20.glVertexAttrib3f(location, value1, value2, value3)
    }

    fun setAttributef(name: String, value1: Float, value2: Float) {
        val location = fetchAttributeLocation(name)
        if (location == -1) return
        GLES20.glVertexAttrib2f(location, value1, value2)
    }

    init {
        requireNotNull(vertexShader) { "vertex shader must not be null" }
        requireNotNull(fragmentShader) { "fragment shader must not be null" }
        vertexShaderSource = vertexShader
        fragmentShaderSource = fragmentShader
        compileShaders(vertexShader, fragmentShader)
        if (isValid) {
            fetchAttributes()
            fetchUniforms()
        }
    }
}