package com.hygeia

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.hygeia.objects.Utilities
import com.hygeia.objects.Utilities.msg
import com.hygeia.objects.Utilities.phoneNumberPattern


class ActQrCodeScannerPhoneNumber: AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_qr_code_scanner)
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)

        codeScanner = CodeScanner(this, scannerView)

        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        codeScanner.decodeCallback = DecodeCallback {qrCodeResult ->
            runOnUiThread{
                when (intent.getStringExtra("From ActQrCodeScannerPhoneNumber")){
                    "ActSendMoney" -> {
                        val intent = Intent(this, ActSendMoney :: class.java)
                        if (phoneNumberPattern.matches(qrCodeResult.text.toString())){
                            val phoneNumber = qrCodeResult.text
                            if (qrCodeResult.text.matches("^09\\d{9}\$".toRegex())){
                                intent.putExtra("qrCodeResult", phoneNumber.substring(1))
                                startActivity(intent)
                                finish()
                            } else {
                                intent.putExtra("qrCodeResult", phoneNumber.substring(3))
                                startActivity(intent)
                                finish()
                            }
                        }else {
                            startActivity(intent)
                            msg("The phone number you scanned is invalid.")
                            finish()
                        }
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
        val intent = Intent(this, ActSendMoney :: class.java)
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(intent)
                finish()
            }
        })
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