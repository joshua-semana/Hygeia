package com.hygeia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.firebase.firestore.FirebaseFirestore
import com.hygeia.objects.MachineManager
import com.hygeia.objects.Utilities.dlgStatus
import com.hygeia.objects.Utilities.isInternetConnected
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


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
        codeScanner.decodeCallback = DecodeCallback {qrCodeResult ->
            runOnUiThread{
                if (isInternetConnected(applicationContext)){
                    toPurchase(qrCodeResult.text)
                } else {
                    dlgStatus(this,"no internet").show()
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

    private fun toPurchase(qrCodeText : String){
        lifecycleScope.launch(Dispatchers.Main){
            val query = machineRef.whereEqualTo("MachineID", qrCodeText).get().await()
            if (!query.isEmpty){
                val document = query.documents[0]
                val machineID = document.getString("MachineID")
                val status = document.getString("Status")
                val name = document.getString("Name")
                val userConnected = document.getLong("User Connected")
                if (status == "Online"){
                    if (userConnected!! < 1){
                        MachineManager.machineId = machineID
                        MachineManager.name = name
                        machineRef.document(machineID.toString()).update("User Connected", 1)
                            .addOnSuccessListener {
                                when (intent.getStringExtra("From ActQrCodeScanner")) {
                                    "ActPurchase" -> startActivity(Intent(applicationContext, ActPurchase::class.java))
                                    "ActPurchaseUsingStars" -> startActivity(Intent(applicationContext, ActPurchaseUsingStars::class.java))
                                    else -> null
                                }
                                finish()
                            }
                    }else{
                        dlgStatus(this@ActQrCodeScanner,"machine offline or in use").apply {
                            setOnDismissListener {
                                finish()
                            }
                        }.show()
                    }
                }else{
                    dlgStatus(this@ActQrCodeScanner,"machine offline or in use").apply {
                        setOnDismissListener {
                            finish()
                        }
                    }.show()
                }
            }else{
                dlgStatus(this@ActQrCodeScanner,"QR code is not registered").apply {
                    setOnDismissListener{
                        finish()
                    }
                }.show()
            }
        }
    }
}