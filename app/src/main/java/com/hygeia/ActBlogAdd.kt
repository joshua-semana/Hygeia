package com.hygeia

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.chip.Chip
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.hygeia.classes.ButtonType
import com.hygeia.databinding.ActBlogAddBinding
import com.hygeia.objects.UserManager
import com.hygeia.objects.Utilities
import com.hygeia.objects.Utilities.dlgConfirmation
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

        with(bind) {
            btnPost.setOnClickListener {
                dlgConfirmation(this@ActBlogAdd, "add blog") {
                    if (it == ButtonType.PRIMARY) {
                        saveBlog()
                    }
                }.show()
            }

            chipGroup.setOnCheckedStateChangeListener { group, _ ->
                val checkedChipId = group.checkedChipId
                checkedChip = findViewById<Chip>(checkedChipId).text.toString()
            }
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
            onBackPressedDispatcher.onBackPressed()
        }
    }
}