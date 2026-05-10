package by.bstu.makovei.greenscan.ui

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

class BarcodeAnalyzer(
    private val onBarcode: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val done = AtomicBoolean(false)
    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_CODE_128
            )
            .build()
    )

    override fun analyze(imageProxy: ImageProxy) {
        if (done.get()) {
            imageProxy.close()
            return
        }
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )
        scanner.process(image)
            .addOnCompleteListener {
                imageProxy.close()
                if (!it.isSuccessful) return@addOnCompleteListener
                val raw = it.result?.firstOrNull { b -> !b.rawValue.isNullOrBlank() }?.rawValue
                    ?: return@addOnCompleteListener
                if (done.compareAndSet(false, true)) {
                    onBarcode(raw)
                }
            }
    }
}
