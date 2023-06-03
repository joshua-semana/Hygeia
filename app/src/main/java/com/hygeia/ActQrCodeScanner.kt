package com.hygeia

import android.content.Intent
import android.health.connect.datatypes.units.Length
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.firebase.firestore.FirebaseFirestore
import com.hygeia.objects.MachineManager
import com.hygeia.objects.Utilities
import com.hygeia.objects.Utilities.dlgStatus
import com.hygeia.objects.Utilities.msg

class ActQrCodeScanner : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner

    private var db = FirebaseFirestore.getInstance()
    private var machineRef = db.collection("Machines")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_qr_code_scanner)
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)

        codeScanner = CodeScanner(this, scannerView)

        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                machineRef.document(it.text).get().addOnSuccessListener { data ->
                    if (data.getString("Status") == "Online"){
                        if (data.getLong("User Connected")!! < 2){
                            MachineManager.machineId = it.text.toString().trim()
                            startActivity(Intent(applicationContext, ActPurchase::class.java))
                            finish()
                        }else {
                            dlgStatus(this,"machine offline or in use").apply {
                                setOnDismissListener {
                                    finish()
                                }
                            }.show()
                        }
                    } else {
                        dlgStatus(this,"machine offline or in use").apply {
                            setOnDismissListener {
                                finish()
                            }
                        }.show()
                    }
                }
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }
}