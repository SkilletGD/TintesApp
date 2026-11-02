package com.skillet.tintes.core.util

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

class BarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    // Configurar ML Kit para que SOLO escanee códigos de barras 1D (lineales de productos)
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_CODE_93,
            Barcode.FORMAT_ITF,
            Barcode.FORMAT_CODABAR
        )
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    // Control de tiempo y candado para prevenir lecturas múltiples seguidas
    private var lastScannedCode: String? = null
    private var lastScannedTimestamp: Long = 0L
    private val isProcessing = AtomicBoolean(false)

    // Cooldown de 3.0 segundos entre lecturas consecutivas
    private val cooldownMs = 3000L

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null && !isProcessing.get()) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    val currentTime = System.currentTimeMillis()
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { code ->
                            val isSameCode = code == lastScannedCode
                            val timePassed = currentTime - lastScannedTimestamp

                            // Solo procesar si transcurrió el tiempo de cooldown o es un código distinto
                            if (!isSameCode || timePassed > cooldownMs) {
                                if (isProcessing.compareAndSet(false, true)) {
                                    lastScannedCode = code
                                    lastScannedTimestamp = currentTime
                                    try {
                                        onBarcodeDetected(code)
                                    } finally {
                                        // Liberar candado después de procesar
                                        isProcessing.set(false)
                                    }
                                }
                            }
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}