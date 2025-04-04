package com.alapan.kmeansfaceclustering

import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

@Composable
fun CameraPreviewView(lifecycleOwner: LifecycleOwner) {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }

    var resultado by remember { mutableStateOf("Esperando detección...") }
    val classifier = remember { KMeansClassifier(context) }
    var useFrontCamera by remember { mutableStateOf(true) }
    var showResultado by remember { mutableStateOf(false) }

    val animatedScale by animateFloatAsState(
        targetValue = if (showResultado) 1.2f else 1f,
        label = "ResultadoScale"
    )

    fun bindCamera(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val cameraSelector = if (useFrontCamera) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(
                    ContextCompat.getMainExecutor(context),
                    FaceAnalyzer(context) { vector ->
                        val esMiRostro = classifier.clasificar(vector)
                        resultado = if (esMiRostro) "✅ Es tu rostro" else "❌ Otro rostro"
                        showResultado = true
                    }
                )
            }

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageAnalysis
        )
    }

    LaunchedEffect(useFrontCamera) {
        val cameraProvider = cameraProviderFuture.get()
        bindCamera(cameraProvider)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = showResultado,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = resultado,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.scale(animatedScale),
                    color = if (resultado.contains("✅")) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }
        }

        IconButton(
            onClick = {
                useFrontCamera = !useFrontCamera
                showResultado = false
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FlipCameraAndroid,
                contentDescription = "Cambiar cámara",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
