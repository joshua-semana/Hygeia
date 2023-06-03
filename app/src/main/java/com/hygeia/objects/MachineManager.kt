package com.hygeia.objects

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.DocumentSnapshot
import com.hygeia.R

object MachineManager {

    var uid: String? = null
    var location: String? = null
    var name: String? = null
    var status: String? = null
    var userConnected: String? = null

    fun setMachineInformation(machineInfo: DocumentSnapshot) {
        with(machineInfo) {
            uid = id
            location = get("Location") as String?
            name = get("Name") as String?
            status = get("Status") as String?
            userConnected = get("User Connected") as String?
        }
    }

    @SuppressLint("SetTextI18n")
    fun dlgEditVendoLocation(context : Context): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dlg_vendo_detail)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val lblDlgVendoDetailEmoji = dialog.findViewById<TextView>(R.id.lblDlgVendoDetailEmoji)
        val txtLayoutDlgVendoDetail = dialog.findViewById<TextInputLayout>(R.id.txtLayoutDlgVendoDetail)
        val lblDlgVendoDetailBody = dialog.findViewById<TextView>(R.id.lblDlgVendoDetailBody)
        val txtDlgVendoDetail = dialog.findViewById<TextInputEditText>(R.id.txtDlgVendoDetail)
        val btnDlgVendoDetailPrimary = dialog.findViewById<Button>(R.id.btnDlgVendoDetailPrimary)
        val btnDlgVendoDetailSecondary = dialog.findViewById<Button>(R.id.btnDlgVendoDetailSecondary)

        lblDlgVendoDetailEmoji.text = Emoji.Edit
        lblDlgVendoDetailBody.text = "${lblDlgVendoDetailBody.text} $name"
        txtDlgVendoDetail.setText(location)

        btnDlgVendoDetailPrimary.setOnClickListener {
            if (txtDlgVendoDetail.text!!.isEmpty()) {
                txtLayoutDlgVendoDetail.error = "Required*"
            } else {
                //TODO
            }
        }

        btnDlgVendoDetailSecondary.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }

    fun dlgEditProduct(context: Context) : Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dlg_product_detail)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        return dialog
    }
}