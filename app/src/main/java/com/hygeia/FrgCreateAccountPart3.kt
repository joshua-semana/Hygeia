package com.hygeia

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hygeia.databinding.FrgCreateAccountPart3Binding

class FrgCreateAccountPart3 : Fragment() {
    private lateinit var bind : FrgCreateAccountPart3Binding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = FrgCreateAccountPart3Binding.inflate(inflater, container, false)

        with(bind) {
            //ELEMENT BEHAVIOR
            tglShowPassword.setOnCheckedChangeListener{ _, isChecked ->
                if (isChecked) {
                    txtReviewPassword.transformationMethod = null
                } else {
                    txtReviewPassword.transformationMethod = PasswordTransformationMethod()
                }
            }
        }

        //NAVIGATION
        bind.btnBackToCreateAccountPart2.setOnClickListener {
            activity?.onBackPressed()
        }

        return bind.root
    }
}