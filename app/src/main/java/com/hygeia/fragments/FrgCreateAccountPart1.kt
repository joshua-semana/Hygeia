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
import android.widget.ArrayAdapter
import android.widget.EditText
import com.hygeia.R
import com.hygeia.objects.Utilities.dlgStatus
import com.hygeia.objects.Utilities.isInternetConnected
import com.hygeia.objects.Utilities.msg
import com.hygeia.databinding.FrgCreateAccountPart1Binding
import java.text.SimpleDateFormat
import java.util.*

class FrgCreateAccountPart1 : Fragment() {
    private lateinit var bind: FrgCreateAccountPart1Binding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bind = FrgCreateAccountPart1Binding.inflate(inflater, container, false)
        with(bind) {
            onResume()

            //MAIN FUNCTIONS
            btnContinue.setOnClickListener {
                if (isInternetConnected(requireContext())) {
                    if (inputsAreNotEmpty()) {
                        sendArguments()
                    } else {
                        dlgStatus(requireContext(), "empty field").show()
                    }
                } else {
                    dlgStatus(requireContext(), "no internet").show()
                }
            }

            cmbGender.setOnItemClickListener { _, _, _, _ ->
                mainLayout.performClick()
            }

            txtBirthdate.setOnClickListener {
                showDatePickerDialog()
            }
            txtLayoutBirthdate.setEndIconOnClickListener {
                showDatePickerDialog()
            }
            //INPUT VALIDATION
            textWatcher(txtFirstName)
            textWatcher(txtLastName)
            //ELEMENT BEHAVIOR
            mainLayout.setOnClickListener {
                requireContext().getSystemService(InputMethodManager::class.java).apply {
                    hideSoftInputFromWindow(requireView().findFocus()?.windowToken, 0)
                }
                mainLayout.requestFocus()
                requireView().findFocus()?.clearFocus()
            }

            //NAVIGATION
            btnBack.setOnClickListener {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }

            return root
        }
    }
    override fun onResume() {
        super.onResume()
        bind.cmbGender.setAdapter(
            ArrayAdapter(
                requireContext(),
                R.layout.elmnt_dropdown_item,
                resources.getStringArray(R.array.gender)
            )
        )
    }
    private fun inputsAreNotEmpty(): Boolean {
        return when {
            bind.txtFirstName.text!!.isEmpty() -> false
            bind.txtLastName.text!!.isEmpty() -> false
            bind.cmbGender.text!!.isEmpty() -> false
            bind.txtBirthdate.text!!.isEmpty() -> false
            else -> true
        }
    }
    //INPUT VALIDATOR
    private fun textWatcher(textField : EditText){
        textField.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString()
                val validInput = input.replace("[^a-zA-Z ]".toRegex(), "")
                if (input != validInput) {
                    textField.setText(validInput)
                    textField.setSelection(textField.text.length)
                    activity?.msg("Only alphabetic characters are allowed.")
                }
            }
            override fun afterTextChanged(s: Editable?){}
        })
    }
    private fun clearTextError() {
        bind.txtLayoutBirthdate.isErrorEnabled = false
    }

    private fun sendArguments() {
        with(bind) {
            val bundle = Bundle().apply {
                putString("firstname", txtFirstName.text?.trim().toString())
                putString("lastname", txtLastName.text?.trim().toString())
                putString("gender", cmbGender.text.toString())
                putString("birthdate", txtBirthdate.text.toString())
            }
            val fragment = FrgCreateAccountPart2().apply { arguments = bundle }
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    androidx.appcompat.R.anim.abc_fade_in,
                    androidx.appcompat.R.anim.abc_fade_out
                )
                .replace(R.id.containerCreateAccount, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun showDatePickerDialog() {
        val currentDate = Calendar.getInstance()
        DatePickerDialog(
            requireContext(), { _, year, month, day ->
                val selectedDate = Calendar.getInstance().apply { set(year, month, day) }
                val age = currentDate.get(Calendar.YEAR) - selectedDate.get(Calendar.YEAR) -
                        if (selectedDate.get(Calendar.DAY_OF_YEAR) > currentDate.get(Calendar.DAY_OF_YEAR)) 1 else 0
                with(bind) {
                    //val date = "$day/${month + 1}/$year"
                    val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                    val formattedDate = dateFormat.format(selectedDate.time)
                    if (age >= 11) {
                        txtBirthdate.setText(formattedDate)
                        clearTextError()
                    } else {
                        txtBirthdate.text?.clear()
                        txtLayoutBirthdate.error = getString(R.string.error_birth_date)
                    }
                }
            }, currentDate.get(Calendar.YEAR) - 11,
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH) - 1
        ).show()
    }
}