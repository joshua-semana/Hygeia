package com.hygeia

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.hygeia.objects.Utilities.dlgConfirmation
import com.hygeia.objects.Utilities.dlgStatus
import java.util.Date

class ActBlogEdit : AppCompatActivity() {
    private lateinit var bind : ActBlogEditBinding
    private lateinit var loading: Dialog

    private var db = FirebaseFirestore.getInstance()
    private var blogsRef = db.collection("Blogs")

    private var checkedChip = BlogManager.type.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActBlogEditBinding.inflate(layoutInflater)
        loading = Utilities.dlgLoading(this@ActBlogEdit)
        setContentView(bind.root)

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
                updatePost()
            }

            btnDeletePost.setOnClickListener {
                deletePost()
            }
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

        dlgConfirmation(this@ActBlogEdit, "delete blog") {
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
}