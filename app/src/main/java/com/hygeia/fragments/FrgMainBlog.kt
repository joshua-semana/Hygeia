package com.hygeia.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.hygeia.ActBlogAdd
import com.hygeia.ActBlogEdit
import com.hygeia.ActBlogView
import com.hygeia.R
import com.hygeia.adapters.ArrAdpBlog
import com.hygeia.classes.DataBlog
import com.hygeia.databinding.FrgMainBlogBinding
import com.hygeia.objects.BlogManager
import com.hygeia.objects.UserManager
import com.hygeia.objects.Utilities

class FrgMainBlog : Fragment(), ArrAdpBlog.OnBlogItemClickListener {

    private lateinit var bind: FrgMainBlogBinding
    private lateinit var listOfBlogs: ArrayList<DataBlog>
    private lateinit var loading: Dialog

    private var db = FirebaseFirestore.getInstance()
    private var blogsRef = db.collection("Blogs")
    private val query = blogsRef.whereEqualTo("isVisible", true)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        bind = FrgMainBlogBinding.inflate(layoutInflater, container, false)
        loading = Utilities.dlgLoading(requireContext())

        constraintViews()

        with (bind) {
            chipAll.isChecked = true
            if (chipAll.isChecked) getListOfBlogs(query)

            chipAll.setOnClickListener {
                getListOfBlogs(query)
            }

            @Suppress("DEPRECATION")
            chipGroup.setOnCheckedChangeListener { _, checkedId ->
                when(checkedId) {
                    R.id.chipAnnouncement -> {
                        getListOfBlogs(query.whereEqualTo("Type", "Announcement"))
                    }
                    R.id.chipInformation -> {
                        getListOfBlogs(query.whereEqualTo("Type", "Information"))
                    }
                    R.id.chipUpdate -> {
                        getListOfBlogs(query.whereEqualTo("Type", "Update"))
                    }
                    R.id.chipHidden -> {
                        getListOfBlogs(blogsRef.whereEqualTo("isVisible", false))
                    }
                }
            }

            fabAddBlog.setOnClickListener {
                startActivity(Intent(requireContext(), ActBlogAdd::class.java))
            }
            return root
        }
    }

    private fun getListOfBlogs(query: Query) {
        listOfBlogs = arrayListOf()
        loading.show()
        bind.listViewBlogs.layoutManager = LinearLayoutManager(requireContext())

        query.orderBy("Date Created", Query.Direction.DESCENDING).get().apply {
            addOnSuccessListener { data ->
                bind.coverListView.visibility = View.GONE
                listOfBlogs.clear()
                if (!data.isEmpty) {
                    for (item in data.documents) {
                        val blog = DataBlog(
                            item.id,
                            item.get("Content").toString(),
                            item.get("Date Created") as Timestamp,
                            item.get("Full Name").toString(),
                            item.get("Title").toString(),
                            item.get("Type").toString(),
                            item.get("isVisible") as Boolean
                        )

                        listOfBlogs.add(blog)
                    }
                    bind.listViewBlogs.adapter = ArrAdpBlog(listOfBlogs, this@FrgMainBlog)
                } else {
                    bind.coverListView.visibility = View.VISIBLE
                }
                loading.dismiss()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        bind.chipAll.isChecked = true
        if (bind.chipAll.isChecked) getListOfBlogs(query)
    }

    override fun onStop() {
        super.onStop()
        bind.chipGroup.clearCheck()
    }

    @SuppressLint("SetTextI18n")
    private fun constraintViews() {
        with(bind) {
            when (UserManager.role) {
                "admin" -> {
                    lblDescription.text = "You can press and hold a post to be able to edit it."
                    fabAddBlog.visibility = View.VISIBLE
                    chipHidden.visibility = View.VISIBLE
                }
                "standard" -> {
                    fabAddBlog.visibility = View.GONE
                    chipHidden.visibility = View.GONE
                }
            }
        }
    }

    override fun onBlogItemClick(ID: String) {
        blogsRef.document(ID).get().addOnSuccessListener { data ->
            BlogManager.setData(data)
            startActivity(Intent(requireContext(), ActBlogView::class.java))
        }
    }

    override fun onBlogItemLongClick(ID: String) {
        if (UserManager.role == "admin") {
            blogsRef.document(ID).get().addOnSuccessListener { data ->
                BlogManager.setData(data)
                startActivity(Intent(requireContext(), ActBlogEdit::class.java))
            }
        }
    }
}