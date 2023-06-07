package com.hygeia.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hygeia.R
import com.hygeia.classes.DataUsers

class ArrAdpUsers(
    private val listMachines: ArrayList<DataUsers>,
    private val clickListener: OnUserClickListener
) : RecyclerView.Adapter<ArrAdpUsers.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.imgListUser)
        val identifier: TextView = itemView.findViewById(R.id.lblListUserIdentifier)
        val role: TextView = itemView.findViewById(R.id.lblListUserRole)
        val btnRole: ImageButton = itemView.findViewById(R.id.btnRoleUpdate)
        val btnStatus: ImageButton = itemView.findViewById(R.id.btnStatusUpdate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.list_users, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return listMachines.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(listMachines[position]) {
                identifier.text = Identifier

                if (Enable == "true") {
                    btnRole.visibility = View.VISIBLE
                    if (Role == "standard") {
                        role.text = "Standard User"
                        image.setImageResource(R.drawable.ic_user_standard)
                        image.setBackgroundResource(R.drawable.bg_circle_50)
                        btnRole.setImageResource(R.drawable.ic_user_promote)
                    } else if (Role == "admin") {
                        role.text = "Administrator"
                        image.setImageResource(R.drawable.ic_user_admin)
                        image.setBackgroundResource(R.drawable.bg_circle_50)
                        btnRole.setImageResource(R.drawable.ic_user_demote)
                    }
                    btnStatus.setImageResource(R.drawable.ic_user_disable)
                } else if (Enable == "false") {
                    btnRole.visibility = View.GONE
                    role.text = "Account Disabled"
                    if (Role == "standard") {
                        image.setImageResource(R.drawable.ic_user_standard_disabled)
                        image.setBackgroundResource(R.drawable.bg_circle_300)
                    } else if (Role == "admin") {
                        image.setImageResource(R.drawable.ic_user_admin_disabled)
                        image.setBackgroundResource(R.drawable.bg_circle_300)
                    }
                    btnStatus.setImageResource(R.drawable.ic_user_enable)
                }

                btnRole.setOnClickListener {
                    clickListener.onUserUpdateRoleClick(UserID!!, Role.toString())
                }

                btnStatus.setOnClickListener {
                    clickListener.onUserUpdateStatusClick(UserID!!, Enable.toString())
                }
            }
        }
    }

    interface OnUserClickListener {
        fun onUserUpdateRoleClick(userID: String, currentRole: String)
        fun onUserUpdateStatusClick(userID: String, isEnabled: String)
    }
}