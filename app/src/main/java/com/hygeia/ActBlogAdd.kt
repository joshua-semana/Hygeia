package com.hygeia

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.google.android.material.chip.Chip
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.hygeia.classes.ButtonType
import com.hygeia.databinding.ActBlogAddBinding
import com.hygeia.objects.UserManager
import com.hygeia.objects.Utilities
import com.hygeia.objects.Utilities.clearTextError
import com.hygeia.objects.Utilities.dlgConfirmation
import com.hygeia.objects.Utilities.isInternetConnected
import com.hygeia.objects.Utilities.showRequiredTextField
import java.util.Date

class ActBlogAdd : AppCompatActivity() {
    private lateinit var bind: ActBlogAddBinding
    private lateinit var loading: Dialog

    private val db = FirebaseFirestore.getInstance()
    private val blogsRef = db.collection("Blogs")

    private var checkedChip = "Announcement"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActBlogAddBinding.inflate(layoutInflater)
        loading = Utilities.dlgLoading(this@ActBlogAdd)
        setContentView(bind.root)

        UserManager.isOnAnotherActivity = true
        UserManager.setUserOnline()

        with(bind) {
            btnPost.setOnClickListener {
                clearTextError(txtLayoutTitle, txtLayoutContent)
                if (inputsAreNotEmpty()) {
                    if (isInternetConnected(this@ActBlogAdd)) {
                        dlgConfirmation(this@ActBlogAdd, "add blog") {
                            if (it == ButtonType.PRIMARY) {
                                saveBlog()
                            }
                        }.show()
                    } else {
                        Utilities.dlgStatus(this@ActBlogAdd, "no internet").show()
                    }
                } else {
                    showRequiredTextField(
                        txtTitle to txtLayoutTitle,
                        txtContent to txtLayoutContent
                    )
                }
            }

            chipGroup.setOnCheckedStateChangeListener { group, _ ->
                val checkedChipId = group.checkedChipId
                checkedChip = findViewById<Chip>(checkedChipId).text.toString()
            }

            mainLayout.setOnClickListener {
                getSystemService(InputMethodManager::class.java).apply {
                    hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                }
                mainLayout.requestFocus()
                currentFocus?.clearFocus()
            }


            btnBack.setOnClickListener {
                onBackBtnPressed()
            }

            onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackBtnPressed()
                }
            })
        }
    }

    private fun saveBlog() {
        val data = hashMapOf(
            "Content" to bind.txtContent.text.toString(),
            "Date Created" to Timestamp(Date()),
            "Full Name" to "${UserManager.firstname} ${UserManager.lastname}",
            "Title" to bind.txtTitle.text.toString(),
            "Type" to checkedChip,
            "Created By" to UserManager.uid,
            "isVisible" to true
        )

        blogsRef.document().set(data).addOnSuccessListener {
            val message = "Posted successfully!"
            val duration = Toast.LENGTH_SHORT
            Toast.makeText(applicationContext, message, duration).show()
            finish()
        }
    }

    private fun inputsAreEmpty(): Boolean {
        return when {
            bind.txtTitle.text!!.isNotEmpty() -> false
            bind.txtContent.text!!.isNotEmpty() -> false
            else -> true
        }
    }

    private fun inputsAreNotEmpty(): Boolean {
        return when {
            bind.txtTitle.text!!.isEmpty() -> false
            bind.txtContent.text!!.isEmpty() -> false
            else -> true
        }
    }

    private fun onBackBtnPressed() {
        if (inputsAreEmpty()) {
            this.finish()
        } else {
            dlgConfirmation(this, "going back") {
                if (it == ButtonType.PRIMARY) {
                    this.finish()
                }
            }.show()
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