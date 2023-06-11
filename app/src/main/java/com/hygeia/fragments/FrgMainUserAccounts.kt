package com.hygeia.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.hygeia.adapters.ArrAdpUsers
import com.hygeia.classes.ButtonType
import com.hygeia.classes.DataUsers
import com.hygeia.databinding.FrgMainUserAccountsBinding
import com.hygeia.objects.Utilities
import com.hygeia.objects.Utilities.dlgConfirmation
import com.hygeia.objects.Utilities.dlgStatus
import com.hygeia.objects.Utilities.msg

class FrgMainUserAccounts : Fragment(), ArrAdpUsers.OnUserClickListener {
    private lateinit var bind: FrgMainUserAccountsBinding
    private lateinit var listOfUsers: ArrayList<DataUsers>
    private lateinit var loading: Dialog

    private var db = FirebaseFirestore.getInstance()
    private var userRef = db.collection("User")

    private val query = userRef.whereIn("role", listOf("standard", "admin"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FrgMainUserAccountsBinding.inflate(inflater, container, false)
        loading = Utilities.dlgLoading(requireContext())

        with(bind) {
            getListOfUsers(query)

            txtLayoutSearch.setEndIconOnClickListener {
                getListOfUsers(query.whereEqualTo("phoneNumber", "${txtLayoutSearch.prefixText}${txtSearch.text.toString()}"))
            }

            mainLayout.setOnClickListener {
                requireContext().getSystemService(InputMethodManager::class.java).apply {
                    hideSoftInputFromWindow(requireView().findFocus()?.windowToken, 0)
                }
                mainLayout.requestFocus()
                requireView().findFocus()?.clearFocus()
            }
            return root
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getListOfUsers(query: Query) {
        bind.cover.visibility = View.VISIBLE

        bind.lblMessage.text = "Fetching user accounts, please wait..."
        bind.loading.visibility = View.VISIBLE
        bind.listViewUsers.layoutManager = LinearLayoutManager(requireContext())
        listOfUsers = arrayListOf()

        query.orderBy("dateCreated", Query.Direction.DESCENDING).get().apply {
            addOnSuccessListener { data ->
                listOfUsers.clear()
                if (!data.isEmpty) {
                    bind.cover.visibility = View.GONE
                    for (item in data.documents) {
                        val items = DataUsers(
                            item.id,
                            item.get("phoneNumber").toString(),
                            item.get("role").toString(),
                            item.get("isEnabled").toString()
                        )
                        listOfUsers.add(items)
                    }
                    bind.listViewUsers.adapter = ArrAdpUsers(
                        listOfUsers, this@FrgMainUserAccounts
                    )
                } else {
                    bind.lblMessage.text = "There are no user matching your search input."
                    bind.loading.visibility = View.GONE
                }
            }
            addOnFailureListener {
                requireContext().msg("Please try again.")
            }
        }
    }

    private fun updateUserRole(userID: String, role: String) {
        loading.show()
        userRef.document(userID).update("role", role).addOnSuccessListener {
            loading.dismiss()
            dlgStatus(requireContext(), "success update user").apply {
                setOnDismissListener {
                    getListOfUsers(query)
                }
                show()
            }
        }
    }

    private fun updateUserStatus(userID: String, status: Boolean) {
        loading.show()
        userRef.document(userID).update("isEnabled", status).addOnSuccessListener {
            loading.dismiss()
            dlgStatus(requireContext(), "success update user").apply {
                setOnDismissListener {
                    getListOfUsers(query)
                }
                show()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        bind.txtSearch.setText("")
    }

    override fun onUserUpdateRoleClick(userID: String, currentRole: String) {
        if (currentRole == "standard") {
            dlgConfirmation(requireContext(), "promote") {
                if (it == ButtonType.PRIMARY) {
                    updateUserRole(userID, "admin")
                }
            }.show()
        } else if (currentRole == "admin") {
            dlgConfirmation(requireContext(), "demote") {
                if (it == ButtonType.PRIMARY) {
                    updateUserRole(userID, "standard")
                }
            }.show()
        }
    }

    override fun onUserUpdateStatusClick(userID: String, isEnabled: String) {
        if (isEnabled == "true") {
            dlgConfirmation(requireContext(), "disable account") {
                if (it == ButtonType.PRIMARY) {
                    updateUserStatus(userID, false)
                }
            }.show()
        } else if (isEnabled == "false") {
            updateUserStatus(userID, true)
        }
    }
}