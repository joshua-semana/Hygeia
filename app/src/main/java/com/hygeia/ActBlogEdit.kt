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
import com.google.firestore.v1.BloomFilter
import com.hygeia.classes.ButtonType
import com.hygeia.classes.DataBlog
import com.hygeia.databinding.ActBlogEditBinding
import com.hygeia.objects.BlogManager
import com.hygeia.objects.UserManager
import com.hygeia.objects.Utilities
import com.hygeia.objects.Utilities.clearTextError
import com.hygeia.objects.Utilities.dlgConfirmation
import com.hygeia.objects.Utilities.dlgStatus
import com.hygeia.objects.Utilities.isInternetConnected
import com.hygeia.objects.Utilities.showRequiredTextField
import java.util.Date

class ActBlogEdit : AppCompatActivity() {
    private lateinit var bind: ActBlogEditBinding
    private lateinit var loading: Dialog

    private var db = FirebaseFirestore.getInstance()
    private var blogsRef = db.collection("Blogs")

    private var checkedChip = BlogManager.type.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActBlogEditBinding.inflate(layoutInflater)
        loading = Utilities.dlgLoading(this@ActBlogEdit)
        setContentView(bind.root)

        UserManager.isOnAnotherActivity = true
        UserManager.setUserOnline()

        with(bind) {
            when (BlogManager.type) {
                "Announcement" -> chipAnnouncement.isChecked = true
                "Information" -> chipInformation.isChecked = true
                "Update" -> chipUpdate.isChecked = true
            }

            txtTitle.setText(BlogManager.title.toString())
            txtContent.setText(BlogManager.content.toString())

            switchBlogVisibility.isChecked = BlogManager.isVisible!!

            chipGroup.setOnCheckedStateChangeListener { group, _ ->
                val checkedChipId = group.checkedChipId
                checkedChip = findViewById<Chip>(checkedChipId).text.toString()
            }

            btnUpdatePost.setOnClickListener {
                clearTextError(txtLayoutTitle, txtLayoutContent)
                if (inputsAreNotEmpty()) {
                    if (isInternetConnected(this@ActBlogEdit)) {
                        if (contentSame()) {
                            Toast.makeText(
                                this@ActBlogEdit,
                                "There are no changes made.",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        } else {
                            updatePost()
                        }
                    } else {
                        dlgStatus(this@ActBlogEdit, "no internet").show()
                    }
                } else {
                    showRequiredTextField(
                        txtTitle to txtLayoutTitle,
                        txtContent to txtLayoutContent
                    )
                }
            }

            btnDeletePost.setOnClickListener {
                if (isInternetConnected(this@ActBlogEdit)) {
                    deletePost()
                } else {
                    dlgStatus(this@ActBlogEdit, "no internet").show()
                }
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

    private fun contentSame(): Boolean {
        return when {
            bind.txtTitle.text.toString() != BlogManager.title -> false
            bind.txtContent.text.toString() != BlogManager.content -> false
            checkedChip != BlogManager.type -> false
            bind.switchBlogVisibility.isChecked != BlogManager.isVisible -> false
            else -> true
        }
    }

    private fun deletePost() {
        dlgConfirmation(this@ActBlogEdit, "delete blog") {
            if (it == ButtonType.PRIMARY) {
                blogsRef.document(BlogManager.uid!!).delete().addOnSuccessListener {
                    dlgStatus(this@ActBlogEdit, "delete blog").apply {
                        setOnDismissListener {
                            finish()
                        }
                        show()
                    }
                }
            }
        }.show()
    }

    private fun updatePost() {
        val data = hashMapOf<String, Any>(
            "Content" to bind.txtContent.text.toString(),
            "Created By" to UserManager.uid!!,
            "Date Created" to Timestamp(Date()),
            "Full Name" to "${UserManager.firstname} ${UserManager.lastname}",
            "Title" to bind.txtTitle.text.toString(),
            "Type" to checkedChip,
            "isVisible" to bind.switchBlogVisibility.isChecked
        )

        dlgConfirmation(this@ActBlogEdit, "update blog") {
            if (it == ButtonType.PRIMARY) {
                blogsRef.document(BlogManager.uid!!.trim()).update(data).addOnSuccessListener {
                    dlgStatus(this@ActBlogEdit, "update blog").apply {
                        setOnDismissListener {
                            finish()
                        }
                        show()
                    }
                }
            }
        }.show()
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