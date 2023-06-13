package com.hygeia

import android.annotation.SuppressLint
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.hygeia.adapters.ArrAdpProducts
import com.hygeia.classes.ButtonType
import com.hygeia.classes.DataProducts
import com.hygeia.databinding.ActPurchaseBinding
import com.hygeia.objects.MachineManager
import com.hygeia.objects.UserManager
import com.hygeia.objects.Utilities.dlgConfirmation
import com.hygeia.objects.Utilities.dlgLoading
import com.hygeia.objects.Utilities.dlgReward
import com.hygeia.objects.Utilities.dlgStatus
import com.hygeia.objects.Utilities.formatCredits
import com.hygeia.objects.Utilities.isInternetConnected
import com.hygeia.objects.Utilities.msg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

class ActPurchase : AppCompatActivity(), ArrAdpProducts.OnProductItemClickListener {
    private lateinit var bind: ActPurchaseBinding
    private lateinit var listOfProducts: ArrayList<DataProducts>
    private lateinit var loading: Dialog
    private lateinit var countDownTimer: CountDownTimer

    private var isVisible = false
    private var isPurchasing = false

    private var db = FirebaseFirestore.getInstance()
    private var machinesRef = db.collection("Machines")
    private var transactionRef = db.collection("Transactions")
    private var userRef = db.collection("User")

    private var subTotal = 0.00
    private var vat = 0.00
    private var grandTotal = 0.00
    private var totalCount = 0
    private var totalRewardStars = 0.00

    private val machineID = MachineManager.machineId
    private var machineName = MachineManager.name

    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    private val usedNumbers = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startCountdownTimer()
        bind = ActPurchaseBinding.inflate(layoutInflater)
        loading = dlgLoading(this@ActPurchase)
        setContentView(bind.root)

        UserManager.isOnAnotherActivity = true
        UserManager.setUserOnline()

        //POPULATE
        getListOfProducts()
        updatePurchaseBreakdown()

        machinesRef.document(machineID!!).addSnapshotListener { snapshot, exception ->
            if (exception != null) return@addSnapshotListener
            if (snapshot?.getString("Status") == "Offline" && isVisible) {
                this@ActPurchase.msg("Vendo is currently offline.")
                finish()
            }
            if (snapshot?.get("isEnabled") == false && isVisible) {
                this@ActPurchase.msg("Error: Vendo is not registered.")
                finish()
            }
        }

        var previousSnapshot: QuerySnapshot? = null
        machinesRef.document(machineID).collection("Products").addSnapshotListener { snapshot, exception ->
            if (exception != null) return@addSnapshotListener
            if (previousSnapshot != null && snapshot!! != previousSnapshot && isVisible && !isPurchasing) {
                this@ActPurchase.msg("Items are updating, please try again.")
                finish()
            }
            previousSnapshot = snapshot
        }

        var prevLocation: String? = null
        machinesRef.document(machineID).addSnapshotListener { snapshot, exception ->
            if (exception != null) return@addSnapshotListener
            if (prevLocation != null && snapshot?.getString("Location") != prevLocation && isVisible) {
                this@ActPurchase.msg("Machine info is updating, please try again.")
                finish()
            }
            prevLocation = snapshot?.getString("Location")
        }

        with(bind) {
            btnPurchase.setOnClickListener {
                if (isInternetConnected(applicationContext)) {
                    if (grandTotal == 0.00) {
                        dlgStatus(this@ActPurchase, "empty cart").show()
                    } else if (grandTotal > UserManager.balance.toString().toDouble()) {
                        dlgStatus(this@ActPurchase, "insufficient funds").show()
                    } else {
                        dlgConfirmation(this@ActPurchase, "purchase") {
                            if (it == ButtonType.PRIMARY) {
                                isPurchasing = true
                                saveTransaction()
                            } else {
                                isPurchasing = false
                            }
                        }.show()
                    }
                } else {
                    dlgStatus(this@ActPurchase, "no internet").show()
                }
            }
            btnBack.setOnClickListener {
                onBackBtnPressed()
            }
            onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackBtnPressed()
                }
            })
        }
    }

    private fun startCountdownTimer() {
        countDownTimer = object : CountDownTimer(3 * 60 * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) { }
            override fun onFinish() {
                this@ActPurchase.msg("You've been kicked out from the machine.")
                finish()
            }
        }
        countDownTimer.start()
    }

    private fun onBackBtnPressed() {
        if (grandTotal == 0.00) {
            machinesRef.document(machineID.toString()).update("User Connected", 0)
                .addOnSuccessListener {
                    finish()
                }
        } else {
            dlgConfirmation(this@ActPurchase, "going back") {
                if (it == ButtonType.PRIMARY) {
                    machinesRef.document(machineID.toString()).update("User Connected", 0)
                        .addOnSuccessListener {
                            finish()
                        }
                }
            }.show()
        }
    }

    private fun saveTransaction() {
        val data = hashMapOf(
            "Amount" to grandTotal,
            "Date Created" to Timestamp(Date()),
            "Reference Number" to createReferenceNumber(),
            "Number" to 0,
            "Type" to "Purchase",
            "User Reference" to UserManager.uid,
            "Vendo" to machineName
        )

        val docRef = transactionRef.document()
        docRef.set(data).addOnSuccessListener {
            val adapter = ArrAdpProducts(listOfProducts, this@ActPurchase)
            for (i in 0 until adapter.itemCount) {
                val viewHolder: RecyclerView.ViewHolder =
                    adapter.createViewHolder(bind.listViewProducts, adapter.getItemViewType(i))
                adapter.bindViewHolder(viewHolder as ArrAdpProducts.ViewHolder, i)

                val product: DataProducts? = adapter.getNonZeroQuantityProduct(i)
                if (product != null) {
                    if (product.Count != 0) {
                        totalCount += product.Count

                        val subData = hashMapOf(
                            "Name" to product.Name,
                            "Price" to product.Price,
                            "Quantity" to product.Count
                        )

                        val database = FirebaseDatabase.getInstance()
                        val reference = database.getReference("Orders")
                        val order =
                            mapOf<String, Any>("Slot_" + product.Slot.toString() to product.Count)
                        reference.updateChildren(order)

                        docRef.collection("Items").document().set(subData)

                        //UPDATE Product Quantity
                        val productsRef =
                            machinesRef.document(machineID.toString()).collection("Products")
                        productsRef.document(product.ID!!)
                            .update("Quantity", product.Quantity!!.toInt() - product.Count)
                    }
                }
            }
            docRef.update("Number", totalCount)
            updateUserBalance()
            MachineManager.dlgLoadingPurchase(this@ActPurchase).apply {
                setOnDismissListener {
                    finishTransaction()
                }
                show()
            }
        }
    }

    private fun updateUserBalance() {
        userRef.document(UserManager.uid!!.trim())
            .update("balance", UserManager.balance.toString().toDouble() - grandTotal)
        userRef.document(UserManager.uid!!.trim())
            .update("points", UserManager.points.toString().toDouble() + totalRewardStars)
        userRef.document(UserManager.uid!!).get().addOnSuccessListener { data ->
            UserManager.updateUserBalance(data)
        }
    }

    private fun finishTransaction() {
        dlgStatus(this@ActPurchase, "success purchase").apply {
            setOnDismissListener {
                dlgReward(this@ActPurchase, totalRewardStars).apply {
                    setOnDismissListener {
                        machinesRef.document(machineID.toString()).update("User Connected", 0)
                            .addOnSuccessListener {
                                finish()
                            }
                    }
                    show()
                }

            }
            show()
        }
    }

    private fun createReferenceNumber(): String {
        val currentDate = dateFormat.format(Date())
        val randomFourDigits = getRandomFourDigits()

        return "$currentDate${String.format("%04d", randomFourDigits)}"
    }

    private fun getRandomFourDigits(): Int {
        val random = Random()
        var randomNum = random.nextInt(10000)
        while (usedNumbers.contains(randomNum)) {
            randomNum = random.nextInt(10000)
        }
        usedNumbers.add(randomNum)
        return randomNum
    }

    private fun updatePurchaseBreakdown() {
        with(bind) {
            lblWalletBalanceNumber.text = formatCredits(UserManager.balance)
            lblSubTotalNumber.text = formatCredits(subTotal)
            lblTaxNumber.text = formatCredits(vat)
            lblGrandTotalNumber.text = formatCredits(grandTotal)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getListOfProducts() {
        bind.cover.visibility = View.VISIBLE

        bind.lblMessage.text = "Fetching products available in this machine, please wait..."
        bind.loading.visibility = View.VISIBLE
        bind.listViewProducts.layoutManager = LinearLayoutManager(this@ActPurchase)
        listOfProducts = arrayListOf()

        machinesRef.document(machineID.toString()).get().apply {
            addOnSuccessListener { parent ->

                machineName = parent.get("Name").toString()
                bind.lblDescVendoID.text = "Vendo No. ${parent.get("Name").toString()}"
                bind.lblDescVendoLocation.text = "Located at ${parent.get("Location").toString()}"

                val productsRef = parent.reference.collection("Products")
                    .orderBy("Slot", Query.Direction.ASCENDING)

                productsRef.get().apply {
                    addOnSuccessListener { child ->
                        listOfProducts.clear()
                        if (!child.isEmpty) {
                            bind.cover.visibility = View.GONE
                            for (item in child.documents) {
                                val product = DataProducts(
                                    item.id,
                                    item.get("Name").toString(),
                                    item.get("Price").toString(),
                                    item.get("Quantity").toString(),
                                    item.get("Slot").toString().toInt(),
                                    item.get("Status").toString().toInt(),
                                    null,
                                    item.get("Reward Points").toString().toDouble()
                                )
                                listOfProducts.add(product)
                            }
                            bind.listViewProducts.adapter = ArrAdpProducts(
                                listOfProducts, this@ActPurchase
                            )
                            if (bind.listViewProducts.adapter?.itemCount == 0) {
                                bind.cover.visibility = View.VISIBLE
                                bind.lblMessage.text =
                                    "Sorry for the inconvenience. There are no available items for this vending machine."
                                bind.loading.visibility = View.GONE
                            }
                        }
                    }
                    addOnFailureListener {
                        this@ActPurchase.msg("Please try again.")
                    }
                }
            }
            addOnFailureListener {
                this@ActPurchase.msg("Please try again.")
            }
        }
    }

    override fun onAddOrMinusClick(productPrice: Double, totalReward: Double) {
        grandTotal = productPrice
        vat = productPrice * 0.12
        subTotal = grandTotal - vat
        totalRewardStars = totalReward
        updatePurchaseBreakdown()
    }

    override fun onStop() {
        super.onStop()
        isVisible = false
        if (isFinishing) {
            machinesRef.document(MachineManager.machineId!!).update("User Connected", 0)
            if (UserManager.isOnAnotherActivity) UserManager.setUserOffline()
        } else {
            machinesRef.document(MachineManager.machineId!!).update("User Connected", 0)
            if (UserManager.isOnAnotherActivity) UserManager.setUserOffline()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        isVisible = true
        machinesRef.document(MachineManager.machineId!!).update("User Connected", 1)
        UserManager.setUserOnline()
    }

    override fun onPause() {
        super.onPause()
        isVisible = false
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
    }
}