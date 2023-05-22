package com.hygeia

import android.app.Activity
import android.os.Parcelable
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.hygeia.Utilities.msg
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

    suspend fun requestOTP(phoneNumber : String, auth : FirebaseAuth, activity: Activity) {
        try {
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {}
                    override fun onVerificationFailed(ex: FirebaseException) {
                        if (ex is FirebaseAuthInvalidCredentialsException) {
                            activity.msg("Invalid request")
                        } else if (ex is FirebaseTooManyRequestsException) {
                            activity.msg("Too many request. Please try again later.")
                        }
//                        OTP.complete(null)
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
            requestOTP(phoneNumber, auth, activity)
        }

        if (requestCount >= requestLimit) {
            delay(requestInterval)
            requestCount = 0
        }
    }
    suspend fun getOTP() : String? {
        return OTP.await()
    }
    suspend fun getToken() : Parcelable?{
        return Token.await()
    }
}