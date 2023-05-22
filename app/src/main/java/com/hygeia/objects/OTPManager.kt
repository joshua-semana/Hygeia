package com.hygeia.objects

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Parcelable
import android.widget.Button
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.hygeia.R
import com.hygeia.classes.ButtonType
import com.hygeia.objects.Utilities.msg
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import kotlin.math.pow

object OTPManager {
    var OTP = CompletableDeferred<String?>()
    var Token = CompletableDeferred<Parcelable?>()

    private var requestCount = 0
    private const val requestLimit = 10
    private const val requestInterval = 1_000L

    suspend fun getOTP(): String? {
        return OTP.await()
    }

    suspend fun getToken(): Parcelable? {
        return Token.await()
    }

    suspend fun requestOTP(activity: Activity, phoneNumber: String, auth: FirebaseAuth) {
        try {
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {}
                    override fun onVerificationFailed(ex: FirebaseException) {
                        if (ex is FirebaseTooManyRequestsException) {
                            activity.msg("Too many request. Please try again later.")
                            OTP.complete(null)
                        }
                    }

                    override fun onCodeSent(
                        otp: String,
                        resendToken: PhoneAuthProvider.ForceResendingToken,
                    ) {
                        OTP.complete(otp)
                        Token.complete(resendToken)
                    }
                }).build()
            PhoneAuthProvider.verifyPhoneNumber(options)
            requestCount++
        } catch (e: FirebaseTooManyRequestsException) {
            delay(requestInterval * (2.0.pow(requestCount.toDouble())).toLong())
            requestOTP(activity, phoneNumber, auth)
        }

        if (requestCount >= requestLimit) {
            delay(requestInterval)
            requestCount = 0
        }
    }

    private fun resendOTP(activity: Activity, phoneNumber: String, auth: FirebaseAuth) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {}
                override fun onVerificationFailed(ex: FirebaseException) {}
                override fun onCodeSent(
                    verificationId: String,
                    resendToken: PhoneAuthProvider.ForceResendingToken,
                ) {
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(
        activity: Activity,
        context: Context,
        phoneNumber: String,
        auth: FirebaseAuth,
        otp: String?,
        onButtonClicked: (type: ButtonType) -> Unit
    ): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dlg_otp)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val lblDlgOtpEmoji = dialog.findViewById<TextView>(R.id.lblDlgOtpEmoji)
        val txtLayoutDlgOtp = dialog.findViewById<TextInputLayout>(R.id.txtLayoutDlgOtp)
        val txtDlgOtp = dialog.findViewById<TextInputEditText>(R.id.txtDlgOtp)
        val btnDlgOtpPrimary = dialog.findViewById<Button>(R.id.btnDlgOtpPrimary)
        val btnDlgOtpSecondary = dialog.findViewById<Button>(R.id.btnDlgOtpSecondary)

        lblDlgOtpEmoji.text = Emoji.Lock

        txtLayoutDlgOtp.setEndIconOnClickListener {
            //Timer Condition
            resendOTP(activity, phoneNumber, auth)
        }

        btnDlgOtpPrimary.setOnClickListener {
            val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                otp!!,
                txtDlgOtp.text.toString()
            )
            auth.signInWithCredential(credential).apply {
                addOnSuccessListener {
                    activity.msg("One-time PIN is correct.")
                    onButtonClicked(ButtonType.VERIFIED)
                    dialog.dismiss()
                }
                addOnFailureListener {
                    txtLayoutDlgOtp.error = context.getString(R.string.error_otp_incorrect)
                }
            }
        }

        btnDlgOtpSecondary.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }
}