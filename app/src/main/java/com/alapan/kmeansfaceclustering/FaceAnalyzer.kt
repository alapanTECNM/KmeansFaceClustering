package com.alapan.kmeansfaceclustering

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceAnalyzer(
    private val context: Context,
    private val onEmbeddingReady: (FloatArray) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .build()

    private val detector = FaceDetection.getClient(options)
    private val embedder = FaceEmbedder(context)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val rotation = imageProxy.imageInfo.rotationDegrees
            val inputImage = InputImage.fromMediaImage(mediaImage, rotation)

            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    val bitmap = imageProxy.toBitmap()
                    if (faces.isNotEmpty()) {
                        val face = faces[0]
                        val box = face.boundingBox

                        try {
                            val cropped = Bitmap.createBitmap(
                                bitmap,
                                box.left.coerceAtLeast(0),
                                box.top.coerceAtLeast(0),
                                box.width().coerceAtMost(bitmap.width - box.left),
                                box.height().coerceAtMost(bitmap.height - box.top)
                            )

                            val vector = embedder.getEmbedding(cropped)
                            onEmbeddingReady(vector)

                        } catch (e: Exception) {
                            Log.e("FaceAnalyzer", "Error al recortar rostro: ${e.message}")
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("FaceAnalyzer", "Error al detectar rostro", it)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}