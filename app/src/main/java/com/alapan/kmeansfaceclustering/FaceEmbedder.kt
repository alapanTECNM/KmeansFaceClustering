package com.alapan.kmeansfaceclustering

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.createScaledBitmap
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class FaceEmbedder(context: Context) {

    private val interpreter: Interpreter

    init {
        val assetFileDescriptor = context.assets.openFd("face_embedding_model.tflite")
        val inputStream = assetFileDescriptor.createInputStream()
        val model = inputStream.readBytes()
        val byteBuffer = ByteBuffer.allocateDirect(model.size).apply {
            order(ByteOrder.nativeOrder())
            put(model)
            rewind()
        }
        interpreter = Interpreter(byteBuffer)
    }

    fun getEmbedding(bitmap: Bitmap): FloatArray {
        val input = preprocessImage(bitmap)
        val output = Array(1) { FloatArray(128) } // salida de 128 floats
        interpreter.run(input, output)
        return output[0]
    }

    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
        val size = 160  // requerido por FaceNet
        val scaled = createScaledBitmap(bitmap, size, size, true)
        val buffer = ByteBuffer.allocateDirect(1 * size * size * 3 * 4).apply {
            order(ByteOrder.nativeOrder())
        }

        for (y in 0 until size) {
            for (x in 0 until size) {
                val pixel = scaled.getPixel(x, y)
                buffer.putFloat(((pixel shr 16 and 0xFF) - 127.5f) / 128f)
                buffer.putFloat(((pixel shr 8 and 0xFF) - 127.5f) / 128f)
                buffer.putFloat(((pixel and 0xFF) - 127.5f) / 128f)
            }
        }

        buffer.rewind()
        return buffer
    }
}
