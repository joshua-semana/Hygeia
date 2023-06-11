package com.hygeia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hygeia.databinding.ActUserInfoBinding
import com.hygeia.objects.UserManager

class ActUserInfo : AppCompatActivity() {
    private lateinit var bind : ActUserInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActUserInfoBinding.inflate(layoutInflater)
        setContentView(bind.root)

        UserManager.isOnAnotherActivity = true
        UserManager.setUserOnline()

        with(bind){
            val fullname = "${UserManager.firstname.toString()} ${UserManager.lastname.toString()}"
            txtFullName.setText(fullname)
            txtGender.setText(UserManager.gender.toString())
            txtBirthDate.setText(UserManager.birthdate.toString())
            txtEmail.setText(UserManager.email.toString())
            txtPhoneNumber.setText(UserManager.phoneNumber.toString())


            btnBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        UserManager.setUserOnline()
    }

    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            if (UserManager.isOnAnotherActivity) UserManager.setUserOffline()
        } else {
            if (UserManager.isOnAnotherActivity) UserManager.setUserOffline()
        }
    }
}