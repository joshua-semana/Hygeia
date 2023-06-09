package com.hygeia

import android.annotation.SuppressLint
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore
import com.hygeia.classes.ButtonType
import com.hygeia.databinding.ActWalletBackgroundsBinding
import com.hygeia.objects.UserManager
import com.hygeia.objects.Utilities
import com.hygeia.objects.Utilities.dlgConfirmation
import com.hygeia.objects.Utilities.dlgStatus
import com.hygeia.objects.Utilities.isInternetConnected

class ActWalletBackgrounds : AppCompatActivity() {
    private lateinit var bind: ActWalletBackgroundsBinding
    private lateinit var loading: Dialog

    private var db = FirebaseFirestore.getInstance()
    private var userRef = db.collection("User")

    private var currentCheckedCard: MaterialCardView? = null

    @SuppressLint("DiscouragedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActWalletBackgroundsBinding.inflate(layoutInflater)
        loading = Utilities.dlgLoading(this@ActWalletBackgrounds)
        setContentView(bind.root)

        with(bind) {
            val cardImageList =
                listOf(cardImage1, cardImage2, cardImage3, cardImage4, cardImage5, cardImage6)

            for (cardImage in cardImageList) {
                if (cardImage.contentDescription == UserManager.walletBackground) {
                    toggleCheckedCard(cardImage)
                }
            }

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

            btnChange.setOnClickListener {
                if (isInternetConnected(this@ActWalletBackgrounds)) {
                    dlgConfirmation(this@ActWalletBackgrounds, "change wallpaper") {
                        loading.show()
                        if (it == ButtonType.PRIMARY) {
                            userRef.document(UserManager.uid!!).update(
                                "walletBackground",
                                currentCheckedCard?.contentDescription.toString()
                            ).addOnSuccessListener {
                                loading.dismiss()
                                UserManager.updateUserBackground(currentCheckedCard?.contentDescription.toString())
                                finish()
                            }
                        }
                    }.show()
                } else {
                    dlgStatus(this@ActWalletBackgrounds, "no internet").show()
                }
            }
            btnBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
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
            val resourceId =
                resources.getIdentifier(contentDescription as String?, "drawable", packageName)
            bind.cardImageBackground.setBackgroundResource(resourceId)
        }
    }
}