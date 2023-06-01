package com.hygeia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.core.text.HtmlCompat
import com.hygeia.databinding.ActCreateAccountBinding
import com.hygeia.objects.Utilities

class ActCreateAccount : AppCompatActivity() {
    private lateinit var bind : ActCreateAccountBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActCreateAccountBinding.inflate(layoutInflater)
        setContentView(bind.root)

        with(bind) {
            lblTermsAndConditions.setOnClickListener {
                Utilities.dlgTermsAndConditions(this@ActCreateAccount).show()
            }
        }
    }
}