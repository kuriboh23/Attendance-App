package com.example.project

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.Result
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CompoundBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.google.zxing.BarcodeFormat
import java.util.*

class ScanActivity : AppCompatActivity() {

    private lateinit var barcodeView: CompoundBarcodeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        barcodeView = findViewById(R.id.barcode_scanner)
        // Set the barcode decoder factory to only accept QR codes
        barcodeView.decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
        barcodeView.initializeFromIntent(intent)

        // Hide the status view (extra text on the screen)
        barcodeView.statusView.visibility = View.GONE

        // Start continuous scanning and callback
        barcodeView.decodeContinuous(callback)
    }

    // BarcodeCallback to handle the scan result
    private val callback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult?) {
            result?.let {
                // Prepare the result to send back to the calling activity
                val data = Intent().apply {
                    putExtra("SCAN_RESULT", it.text) // Send the scanned text back
                }

                // Return the result to the calling activity
                setResult(Activity.RESULT_OK, data)
                finish() // Close the scanning activity
            }
        }

        override fun possibleResultPoints(resultPoints: MutableList<com.google.zxing.ResultPoint>?) {}
    }

    // Resume scanning when the activity resumes
    override fun onResume() {
        super.onResume()
        barcodeView.resume()  // Start scanning when the activity resumes
    }

    // Pause scanning when the activity pauses
    override fun onPause() {
        super.onPause()
        barcodeView.pause()  // Pause scanning to save resources when not in use
    }
}
