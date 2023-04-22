package com.hygeia.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.hygeia.R
import com.hygeia.Utilities.msg
import com.hygeia.databinding.FrgCreateAccountPart1Binding
import java.util.*

class FrgCreateAccountPart1 : Fragment() {
    private lateinit var bind : FrgCreateAccountPart1Binding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = FrgCreateAccountPart1Binding.inflate(inflater, container, false)

        with(bind) {
            //MAIN FUNCTIONS
            txtNewBirthDate.setOnClickListener {
                btnCalendar.performClick()
                clearElementFocus()
            }
            btnCalendar.setOnClickListener { showDatePickerDialog() }

            //ELEMENT BEHAVIOR
            mainLayout.setOnClickListener {
                clearElementFocus()
            }

            //INPUT VALIDATION
            textWatcher(txtNewFirstName)
            textWatcher(txtNewLastName)
            textWatcher(txtNewBirthDate)

            //NAVIGATION
            btnContinueCreateAccountToPart2.setOnClickListener {
                sendArguments()
            }

            btnBackToLogin.setOnClickListener {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }

            return root
        }
    }

    private fun sendArguments() {
        with(bind) {
            val bundle = Bundle().apply {
                putString("gender", cmbGender.selectedItem.toString())
                putString("firstname", txtNewFirstName.text.toString())
                putString("lastname", txtNewLastName.text.toString())
                putString("birthdate", txtNewBirthDate.text.toString())
            }
            val fragment = FrgCreateAccountPart2().apply { arguments = bundle }
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                .replace(R.id.containerCreateAccount, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun showDatePickerDialog() {
        val currentDate = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            val selectedDate = Calendar.getInstance().apply { set(year, month, day) }
            val age = currentDate.get(Calendar.YEAR) - selectedDate.get(Calendar.YEAR) -
                    if (selectedDate.get(Calendar.DAY_OF_YEAR) > currentDate.get(Calendar.DAY_OF_YEAR)) 1 else 0
            with(bind) {
                val date = "$day/${month + 1}/$year"
                if (age >= 11) {
                    txtNewBirthDate.setText(date)
                    txtNewBirthDate.setBackgroundResource(R.drawable.bg_textfield_default)
                    lblCreateAccountErrorMsg1.visibility = View.GONE
                } else {
                    txtNewBirthDate.text.clear()
                    txtNewBirthDate.setBackgroundResource(R.drawable.bg_textfield_error)
                    lblCreateAccountErrorMsg1.visibility = View.VISIBLE
                }
            }
        }, currentDate.get(Calendar.YEAR) - 11,
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH) - 1).show()
    }

    private fun clearElementFocus() {
        requireContext().getSystemService(InputMethodManager::class.java).apply {
            hideSoftInputFromWindow(requireView().findFocus()?.windowToken, 0)
        }
        bind.mainLayout.requestFocus()
        requireView().findFocus()?.clearFocus()
    }

    //INPUT VALIDATOR
    private fun textWatcher(textField : EditText) {
        with(bind) {
            textField.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (textField.hint.toString() == "Enter first name" && textField.hint.toString() == "Enter last name") {
                        if (s.toString().isNotEmpty() && !s.toString().matches("[a-zA-Z]+".toRegex())) {
                            textField.setText(s?.substring(0, s.length - 1))
                            textField.setSelection(textField.text.length)
                            activity?.msg("Only alphabetic characters are allowed.")
                        }
                    }
                }
                override fun afterTextChanged(s: Editable?) {
                    btnContinueCreateAccountToPart2.isEnabled = listOf(
                        txtNewFirstName,
                        txtNewLastName,
                        txtNewBirthDate
                    ).all { it.text.isNotEmpty() }
                }
            })
        }
    }
}