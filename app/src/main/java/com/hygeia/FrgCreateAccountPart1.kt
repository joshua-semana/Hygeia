package com.hygeia

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import androidx.navigation.Navigation
import com.hygeia.databinding.FrgCreateAccountPart1Binding
import java.util.*

class FrgCreateAccountPart1 : Fragment() {
    private lateinit var bind : FrgCreateAccountPart1Binding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = FrgCreateAccountPart1Binding.inflate(inflater, container, false)

        with(bind) {
            txtNewBirthDate.setOnClickListener { btnCalendar.performClick() }
            btnCalendar.setOnClickListener { showDatePickerDialog() }

            //ELEMENT BEHAVIOR
            mainLayout.setOnClickListener {
                requireActivity().getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(requireView().findFocus()?.windowToken, 0)
                mainLayout.requestFocus()
                requireView().findFocus()?.clearFocus()
            }

            //VALIDATION
            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
                override fun afterTextChanged(s: Editable?) {
                    btnContinueCreateAccountToPart2.isEnabled = txtNewFirstName.text.isNotEmpty() and
                            txtNewLastName.text.isNotEmpty() and
                            txtNewBirthDate.text.isNotEmpty()
                }
            }
            txtNewFirstName.addTextChangedListener(textWatcher)
            txtNewLastName.addTextChangedListener(textWatcher)
            txtNewBirthDate.addTextChangedListener(textWatcher)
        }

        //NAVIGATION
        bind.btnContinueCreateAccountToPart2.setOnClickListener {
            Navigation.findNavController(bind.root).navigate(R.id.CreateAccountPart1ToPart2)
        }

        bind.btnBackToLogin.setOnClickListener {
            activity?.onBackPressed()
        }

        return bind.root
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val calMonth = calendar.get(Calendar.MONTH)
        val calDay = calendar.get(Calendar.DAY_OF_MONTH)
        val calYear = calendar.get(Calendar.YEAR)
        val dlgDatePicker = DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                val date = (dayOfMonth.toString() + "/" + (monthOfYear + 1) + "/" + year)
                bind.txtNewBirthDate.setText(date)
            },
            calYear, calMonth, calDay
        )
        dlgDatePicker.show()
    }
}