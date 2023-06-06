package com.hygeia

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.card.MaterialCardView
import com.hygeia.databinding.ActWalletBackgroundsBinding

class ActWalletBackgrounds : AppCompatActivity() {
    private lateinit var bind : ActWalletBackgroundsBinding

    private var currentCheckedCard: MaterialCardView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActWalletBackgroundsBinding.inflate(layoutInflater)
        setContentView(bind.root)

        with(bind) {
            cardImage1.setOnClickListener {
                toggleCheckedCard(cardImage1)
            }

            cardImage2.setOnClickListener {
                toggleCheckedCard(cardImage2)
            }

            cardImage3.setOnClickListener {
                toggleCheckedCard(cardImage3)
            }

            cardImage4.setOnClickListener {
                toggleCheckedCard(cardImage4)
            }

            cardImage5.setOnClickListener {
                toggleCheckedCard(cardImage5)
            }

            cardImage6.setOnClickListener {
                toggleCheckedCard(cardImage6)
            }
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun toggleCheckedCard(cardView: MaterialCardView) {
        if (!cardView.isChecked) {
            currentCheckedCard?.isChecked = false
            cardView.isChecked = true
            currentCheckedCard = cardView

            val contentDescription = cardView.contentDescription
            val resourceId = resources.getIdentifier(contentDescription as String?, "drawable", packageName)
            bind.cardImageBackground.setBackgroundResource(resourceId)
        }
    }
}