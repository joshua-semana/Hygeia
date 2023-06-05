package com.hygeia.objects

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.widget.Button
import android.widget.TextView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.hygeia.R
import com.hygeia.classes.ButtonType
import com.hygeia.objects.Utilities.clearTextError

object MachineManager {

    val machineRef = FirebaseFirestore.getInstance().collection("Machines")

    var uid: String? = null
    var location: String? = null
    var name: String? = null
    var status: String? = null
    var userConnected: Long? = null
    var machineId: String? = null

    fun setMachineInformation(machineInfo: DocumentSnapshot) {
        with(machineInfo) {
            uid = id
            location = get("Location") as String?
            name = get("Name") as String?
            status = get("Status") as String?
            userConnected = get("User Connected") as Long?
            machineId = get("MachineID") as String?

        }
    }

    @SuppressLint("SetTextI18n")
    fun dlgEditVendoLocation(
        context: Context,
        onButtonClicked: (type: ButtonType) -> Unit
    ): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dlg_vendo_detail)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val lblDlgVendoDetailEmoji = dialog.findViewById<TextView>(R.id.lblDlgVendoDetailEmoji)
        val txtLayoutDlgVendoDetail =
            dialog.findViewById<TextInputLayout>(R.id.txtLayoutDlgVendoDetail)
        val lblDlgVendoDetailBody = dialog.findViewById<TextView>(R.id.lblDlgVendoDetailBody)
        val txtDlgVendoDetail = dialog.findViewById<TextInputEditText>(R.id.txtDlgVendoDetail)
        val btnDlgVendoDetailPrimary = dialog.findViewById<Button>(R.id.btnDlgVendoDetailPrimary)
        val btnDlgVendoDetailSecondary =
            dialog.findViewById<Button>(R.id.btnDlgVendoDetailSecondary)

        lblDlgVendoDetailEmoji.text = Emoji.Edit
        lblDlgVendoDetailBody.text = "${lblDlgVendoDetailBody.text} $name"
        txtDlgVendoDetail.setText(location)

        btnDlgVendoDetailPrimary.setOnClickListener {
            if (txtDlgVendoDetail.text!!.isEmpty()) {
                txtLayoutDlgVendoDetail.error = "Required*"
            } else {
                machineRef.document(uid!!)
                    .update("Location", txtDlgVendoDetail.text.toString()).addOnSuccessListener {
                    onButtonClicked(ButtonType.PRIMARY)
                    dialog.dismiss()
                }
            }
        }

        btnDlgVendoDetailSecondary.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }

    @SuppressLint("SuspiciousIndentation")
    fun dlgEditProduct(
        context: Context,
        productID: String,
        onButtonClicked: (type: ButtonType) -> Unit
    ): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dlg_product_detail)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val lblDlgProductDetailEmoji = dialog.findViewById<TextView>(R.id.lblDlgProductDetailEmoji)
        val txtLayoutDlgProductName = dialog.findViewById<TextInputLayout>(R.id.txtLayoutDlgProductName)
        val txtLayoutDlgProductPrice = dialog.findViewById<TextInputLayout>(R.id.txtLayoutDlgProductPrice)
        val txtLayoutDlgProductQuantity = dialog.findViewById<TextInputLayout>(R.id.txtLayoutDlgProductQuantity)
        val txtDlgProductName = dialog.findViewById<TextInputEditText>(R.id.txtDlgProductName)
        val txtDlgProductPrice = dialog.findViewById<TextInputEditText>(R.id.txtDlgProductPrice)
        val txtDlgProductQuantity = dialog.findViewById<TextInputEditText>(R.id.txtDlgProductQuantity)
        val btnDlgProductDetailPrimary = dialog.findViewById<Button>(R.id.btnDlgProductDetailPrimary)
        val btnDlgProductDetailSecondary = dialog.findViewById<Button>(R.id.btnDlgProductDetailSecondary)
        val switchVendoSlotStatus = dialog.findViewById<SwitchMaterial>(R.id.switchVendoSlotStatus)

        lblDlgProductDetailEmoji.text = Emoji.Edit

        machineRef.document(uid!!.trim()).get().addOnSuccessListener { parent ->
            parent.reference.collection("Products").document(productID).get()
                .addOnSuccessListener { child ->
                    txtDlgProductName.setText(child.getString("Name"))
                    txtDlgProductPrice.setText(child.get("Price").toString())
                    txtDlgProductQuantity.setText(child.get("Quantity").toString())
                    switchVendoSlotStatus.isChecked = child.get("Status").toString() == "1"
                }
        }

        btnDlgProductDetailPrimary.setOnClickListener {
            clearTextError(
                txtLayoutDlgProductName,
                txtLayoutDlgProductPrice,
                txtLayoutDlgProductQuantity
            )
            if (txtDlgProductName.text!!.isNotEmpty() &&
                txtDlgProductPrice.text!!.isNotEmpty() &&
                txtDlgProductQuantity.text!!.isNotEmpty()
            ) {
                if (txtDlgProductQuantity.text.toString().toLong() > 10) {
                    txtLayoutDlgProductQuantity.error =
                        "The maximum items per product is less than or equal to 10."
                } else {
                    val productUpdatedData = hashMapOf<String, Any>(
                        "Name" to txtDlgProductName.text.toString(),
                        "Price" to txtDlgProductPrice.text.toString().toDouble(),
                        "Quantity" to txtDlgProductQuantity.text.toString().toLong(),
                        "Status" to if (switchVendoSlotStatus.isChecked) 1 else 0
                    )
                    machineRef.document(uid!!.trim()).get().addOnSuccessListener { parent ->
                        parent.reference.collection("Products").document(productID)
                            .update(productUpdatedData).addOnSuccessListener {
                                onButtonClicked(ButtonType.PRIMARY)
                                dialog.dismiss()
                            }
                    }
                }
            } else {
                Utilities.showRequiredTextField(
                    txtDlgProductName to txtLayoutDlgProductName,
                    txtDlgProductPrice to txtLayoutDlgProductPrice,
                    txtDlgProductQuantity to txtLayoutDlgProductQuantity
                )
            }
        }
        btnDlgProductDetailSecondary.setOnClickListener {
            dialog.dismiss()
        }
        return dialog
    }


}