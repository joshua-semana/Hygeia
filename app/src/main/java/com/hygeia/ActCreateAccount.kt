package com.hygeia

import android.app.DatePickerDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.hygeia.classes.ButtonType
import com.hygeia.databinding.ActCreateAccountBinding
import com.hygeia.objects.OTPManager
import com.hygeia.objects.Utilities
import com.hygeia.objects.Utilities.clearTextError
import com.hygeia.objects.Utilities.dlgLoading
import com.hygeia.objects.Utilities.dlgStatus
import com.hygeia.objects.Utilities.isInternetConnected
import com.hygeia.objects.Utilities.msg
import com.hygeia.objects.Utilities.showRequiredComboBox
import com.hygeia.objects.Utilities.showRequiredTextField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ActCreateAccount : AppCompatActivity() {
    private lateinit var bind : ActCreateAccountBinding
    private lateinit var auth: FirebaseAuth

    private val db = FirebaseFirestore.getInstance()
    private val userRef = db.collection("User")
    private lateinit var loading : Dialog

    private var firstname = ""
    private var lastname = ""
    private var gender = ""
    private var birthdate = ""
    private var emailAddress = ""
    private var phoneNumber = ""
    private var password = ""
    private var confirmPassword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActCreateAccountBinding.inflate(layoutInflater)
        auth = Firebase.auth
        loading = dlgLoading(this@ActCreateAccount)
        setContentView(bind.root)

        with(bind) {
            //cmbGender Adapter
            onResume()
            //MAIN FUNCTION
            btnContinue.setOnClickListener {
                clearTextError(
                    txtLayoutFirstName,
                    txtLayoutLastName,
                    txtLayoutGender,
                    txtLayoutBirthdate,
                    txtLayoutEmail,
                    txtLayoutPhoneNumber,
                    txtLayoutPassword,
                    txtLayoutConfirmPassword
                )
                if (isInternetConnected(this@ActCreateAccount)){
                    if (inputsAreNotEmpty()){
                        loading.show()
                        //Get data for input validation and for map
                        firstname = txtFirstName.text.toString()
                        lastname = txtLastName.text.toString()
                        gender = cmbGender.text.toString()
                        birthdate = txtBirthdate.text.toString()
                        emailAddress = txtEmail.text.toString()
                        phoneNumber = (txtLayoutPhoneNumber.prefixText.toString() + txtPhoneNumber.text.toString()).trim()
                        password = txtPassword.text.toString()
                        confirmPassword = txtConfirmPassword.text.toString()

                        lifecycleScope.launch(Dispatchers.Main) {
                            if (inputsAreCorrect()){
                                processOTP()
                            }
                            loading.dismiss()
                        }
                    } else {
                        showRequiredTextField(
                            txtFirstName to txtLayoutFirstName,
                            txtLastName to txtLayoutLastName,
                            txtBirthdate to txtLayoutBirthdate,
                            txtEmail to txtLayoutEmail,
                            txtPhoneNumber to txtLayoutPhoneNumber,
                            txtPassword to txtLayoutPassword,
                            txtConfirmPassword to txtLayoutConfirmPassword,
                        )
                        showRequiredComboBox(
                            cmbGender to txtLayoutGender
                        )
                    }
                } else {
                    dlgStatus(this@ActCreateAccount, "no internet").show()
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
            //VALIDATE INPUT
            textWatcher(txtFirstName)
            textWatcher(txtLastName)
            //ELEMENT BEHAVIOR
            mainLayout.setOnClickListener {
                getSystemService(InputMethodManager::class.java).apply {
                    hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                }
                mainLayout.requestFocus()
                currentFocus?.clearFocus()
            }
            //NAVIGATION
            btnBack.setOnClickListener {
                onBackBtnPressed()
            }
            onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackBtnPressed()
                }
            })
            //TERMS & CONDITION DIALOG
            lblTermsAndConditions.setOnClickListener {
                Utilities.dlgTermsAndConditions(this@ActCreateAccount).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        bind.cmbGender.setAdapter(
            ArrayAdapter(
                this,
                R.layout.elmnt_dropdown_item,
                resources.getStringArray(R.array.gender)
            )
        )
    }

    private suspend fun processOTP() {
        OTPManager.requestOTP(this@ActCreateAccount, phoneNumber, auth)
        if (OTPManager.getOTP() != null) {
            OTPManager.verifyOtp(
                this@ActCreateAccount,
                this@ActCreateAccount,
                phoneNumber,
                auth,
                OTPManager.getOTP()
            ) {
                if (it == ButtonType.VERIFIED) {
                    loading.dismiss()
                    createAccount()
                }
            }.show()
        }
    }

    private fun createAccount() {
        loading.show()
        val convertedFirstName = firstname.lowercase(Locale.getDefault()).capitalize(Locale.getDefault())
        val convertedLastName = lastname.lowercase(Locale.getDefault()).capitalize(Locale.getDefault())
        val userData = hashMapOf(
            "gender" to gender,
            "firstname" to convertedFirstName,
            "lastname" to convertedLastName,
            "birthdate" to birthdate,
            "email" to emailAddress,
            "phoneNumber" to phoneNumber,
            "password" to password,
            "status" to "inactive",
            "role" to "standard",
            "balance" to 0,
            "dateCreated" to Timestamp(Date()),
            "points" to 1
        )

        auth.createUserWithEmailAndPassword(emailAddress, password)
            .addOnSuccessListener { result ->
                loading.dismiss()
                userRef.document(result.user!!.uid).set(userData)
                dlgStatus(this@ActCreateAccount, "success create account").apply {
                    setOnDismissListener {
                        this@ActCreateAccount.finish()
                    }
                    show()
                }
            }
    }

    private fun onBackBtnPressed(){
        if (inputsAreEmpty()) {
            this.finish()
        } else {
            Utilities.dlgConfirmation(this, "going back") {
                if (it == ButtonType.PRIMARY) {
                    this.finish()
                }
            }.show()
        }
    }

    private fun inputsAreEmpty(): Boolean {
        return when {
            bind.txtFirstName.text!!.isNotEmpty() -> false
            bind.txtLastName.text!!.isNotEmpty() -> false
            bind.cmbGender.text!!.isNotEmpty() -> false
            bind.txtBirthdate.text!!.isNotEmpty() -> false
            bind.txtEmail.text!!.isNotEmpty() -> false
            bind.txtPhoneNumber.text!!.isNotEmpty() -> false
            bind.txtPassword.text!!.isNotEmpty() -> false
            bind.txtConfirmPassword.text!!.isNotEmpty() -> false
            else -> true
        }
    }

    private fun inputsAreNotEmpty(): Boolean {
        return when {
            bind.txtFirstName.text!!.isEmpty() -> false
            bind.txtLastName.text!!.isEmpty() -> false
            bind.cmbGender.text!!.isEmpty() -> false
            bind.txtBirthdate.text!!.isEmpty() -> false
            bind.txtEmail.text!!.isEmpty() -> false
            bind.txtPhoneNumber.text!!.isEmpty() -> false
            bind.txtPassword.text!!.isEmpty() -> false
            bind.txtConfirmPassword.text!!.isEmpty() -> false
            else -> true
        }
    }

    private suspend fun inputsAreCorrect(): Boolean {
        var inputErrorCount = 0
        with(bind) {
            if (emailAddress.matches(Utilities.emailPattern)) {
                if (getEmailExistenceOf(emailAddress)) {
                    inputErrorCount++
                    txtLayoutEmail.error = getString(R.string.error_email_taken)
                }
            } else {
                inputErrorCount++
                txtLayoutEmail.error = getString(R.string.error_email_format)
            }

            if (phoneNumber.matches(Utilities.phoneNumberPattern)) {
                if (getPhoneNumberExistenceOf(phoneNumber)) {
                    inputErrorCount++
                    txtLayoutPhoneNumber.error = getString(R.string.error_phone_taken)
                }
            } else {
                inputErrorCount++
                txtLayoutPhoneNumber.error = getString(R.string.error_phone_format)
            }

            if (!password.matches(Utilities.passwordPattern)) {
                inputErrorCount++
                txtLayoutPassword.error = getString(R.string.error_password_format)
            }

            if (confirmPassword != password) {
                inputErrorCount++
                txtLayoutConfirmPassword.error = getString(R.string.error_password_matched)
            }
        }
        return inputErrorCount == 0
    }
    private suspend fun getEmailExistenceOf(email: String): Boolean {
        val query = userRef.whereEqualTo("email", email).get().await()
        return !query.isEmpty
    }
    private suspend fun getPhoneNumberExistenceOf(phoneNumber: String): Boolean {
        val query = userRef.whereEqualTo("phoneNumber", phoneNumber).get().await()
        return !query.isEmpty
    }

    private fun showDatePickerDialog() {
        val currentDate = Calendar.getInstance()
        DatePickerDialog(
            this, { _, year, month, day ->
                val selectedDate = Calendar.getInstance().apply { set(year, month, day) }
                val age = currentDate.get(Calendar.YEAR) - selectedDate.get(Calendar.YEAR) -
                        if (selectedDate.get(Calendar.DAY_OF_YEAR) > currentDate.get(Calendar.DAY_OF_YEAR)) 1 else 0
                with(bind) {
                    //val date = "$day/${month + 1}/$year"
                    val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                    val formattedDate = dateFormat.format(selectedDate.time)
                    if (age >= 11) {
                        txtBirthdate.setText(formattedDate)
                        clearTextError(txtLayoutBirthdate)
                    } else {
                        txtBirthdate.text?.clear()
                        txtLayoutBirthdate.error = getString(R.string.error_birth_date)
                    }
                }
            }, currentDate.get(Calendar.YEAR) - 11,
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH) - 1
        ).apply {
            datePicker.maxDate = currentDate.timeInMillis
            show()
        }
    }
    //INPUT VALIDATION
    private fun textWatcher(textField : EditText){
        textField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString()
                val validInput = input.replace("[^a-zA-Z ]".toRegex(), "")
                if (input != validInput) {
                    textField.setText(validInput)
                    textField.setSelection(textField.text.length)
                    this@ActCreateAccount.msg("Only alphabetic characters are allowed.")
                }
            }
            override fun afterTextChanged(s: Editable?){}
        })
    }
}