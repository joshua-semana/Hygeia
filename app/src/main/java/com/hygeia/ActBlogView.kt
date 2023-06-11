package com.hygeia

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.hygeia.databinding.ActBlogViewBinding
import com.hygeia.objects.BlogManager
import com.hygeia.objects.UserManager

class ActBlogView : AppCompatActivity() {
    private lateinit var bind : ActBlogViewBinding
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActBlogViewBinding.inflate(layoutInflater)
        setContentView(bind.root)

        UserManager.isOnAnotherActivity = true
        UserManager.setUserOnline()

        with(bind) {
            lblBlogContent.text = BlogManager.content
            lblBlogTitle.text = BlogManager.title
            lblBlogType.text = BlogManager.type
            lblBlogCreatedBy.text = "Posted by ${BlogManager.fullname}"
            lblBlogDateTime.text = "${BlogManager.dateString} at ${BlogManager.timeString}"

            when (BlogManager.type) {
                "Announcement" -> badge.setBackgroundResource(R.drawable.badge_yellow)
                "Information" -> badge.setBackgroundResource(R.drawable.badge_green)
                "Update" -> badge.setBackgroundResource(R.drawable.badge_blue)
            }

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