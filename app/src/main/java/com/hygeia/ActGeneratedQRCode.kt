package com.hygeia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.hygeia.objects.Utilities.generateQRCode

class ActGeneratedQRCode : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_generated_qrcode)

        val qrCodeImageView = findViewById<ImageView>(R.id.imgQrCode)
        val qrCodeData = "+639293866080"
        val qrCodeBitmap = generateQRCode(qrCodeData)


        qrCodeImageView.setImageBitmap(qrCodeBitmap)
    }
}