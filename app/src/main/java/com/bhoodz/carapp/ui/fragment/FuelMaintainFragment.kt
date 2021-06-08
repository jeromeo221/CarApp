package com.bhoodz.carapp.ui.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.Observer
import com.bhoodz.carapp.R
import com.bhoodz.carapp.helpers.MathHelper
import com.bhoodz.carapp.models.FUEL_DATE_DISPLAY
import com.bhoodz.carapp.models.Fuel
import com.bhoodz.carapp.viewmodels.FuelViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_fuel_maintain.*
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_FUEL = "fuel"
private const val ARG_VEHICLE_ID = "vehicleId"
private const val ARG_TOKEN = "token"
//private const val TAG = "FuelMaintainFragment"

/**
 * A simple [Fragment] subclass.
 * Use the [FuelMaintainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FuelMaintainFragment : MaintainFragment() {
    private var fuel: Fuel? = null
    private var vehicleId: String? = null
    private var token: String? = null
    private var listener: OnFuelMaintainSave? = null
    private val fuelViewModel by lazy { ViewModelProviders.of(requireActivity()).get(FuelViewModel::class.java) }

    interface OnFuelMaintainSave {
        fun onFuelMaintainSave(fuel: Fuel, isAdd: Boolean)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFuelMaintainSave) {
            listener = context
        } else {
            throw RuntimeException("$context must implement onFuelMaintainSave")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            fuel = it.getParcelable(ARG_FUEL)
            vehicleId = it.getString(ARG_VEHICLE_ID)
            token = it.getString(ARG_TOKEN)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fuel_maintain, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fuelViewModel.cleaErrorMessageMaintain()
        fuelViewModel.errorMessageMaintainEntry.observe(viewLifecycleOwner, Observer { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        })

        val fuel = fuel
        if (fuel == null) {
            //Add a new fuel
            txtFuelDateTime.setText(Fuel.retrieveTodayForDisplay())
        } else {
            //Update a fuel
            val dateString = Fuel.prepareDateToDisplay(fuel.date!!)
            txtFuelDateTime.setText(dateString)
            txtFuelDateTime.isEnabled = false
            txtFuelOdometer.setText(fuel.odometer.toString())
            txtFuelVolume.setText(String.format(Locale.getDefault(), fuel.volume.toString()))
            txtFuelPrice.setText(String.format(Locale.getDefault(), fuel.price.toString()))
            txtFuelCost.setText(String.format(Locale.getDefault(), fuel.cost.toString()))
            swFuelIsFull.isChecked = fuel.isFull
            swFuelIsMissed.isChecked = fuel.isMissed
            swFuelIsOdoEst.isChecked = fuel.isEstOdo
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        with (activity as AppCompatActivity) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = if (fuel == null) {
                getString(R.string.title_fragment_fuel_add)
            } else {
                getString(R.string.title_fragment_fuel_edit)
            }
            invalidateOptionsMenu()
            fab.hide()
            vehicleFragment.visibility = View.INVISIBLE
        }

        txtFuelDateTime.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) return@setOnFocusChangeListener

            val calendar = Calendar.getInstance()

            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)

                    val dateFormat = SimpleDateFormat(FUEL_DATE_DISPLAY, Locale.getDefault())
                    txtFuelDateTime.setText(dateFormat.format(calendar.time))
                }

                val timePicker = TimePickerDialog(requireContext(),
                    timeSetListener,
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false)
                timePicker.show()
            }

            val datePicker = DatePickerDialog(requireContext(),
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }

        btnFuelSave.setOnClickListener {
            if (validate()) {
                save()
            }
        }
    }

    private fun validate(): Boolean {
        if (txtFuelDateTime.text.isEmpty()) {
            txtFuelDateTime.error = "Fuel Data/Time is required"
            return false
        }
        if (!Fuel.isValidDateUserInput(txtFuelDateTime.text.toString())) {
            txtFuelDateTime.error = "Incorrect Date and Time format"
            return false
        }
        if (txtFuelOdometer.text.isEmpty()) {
            txtFuelOdometer.error = "Fuel Odometer is required"
            return false
        }
        if (txtFuelVolume.text.isEmpty()) {
            txtFuelVolume.error = "Fuel Volume is required"
            return false
        }
        if (txtFuelPrice.text.isEmpty()) {
            txtFuelPrice.error = "Fuel Price is required"
            return false
        }
        if (txtFuelCost.text.isEmpty()) {
            txtFuelCost.error = "Fuel Cost is required"
            return false
        }

        val volume = txtFuelVolume.text.toString().toBigDecimal()
        val price = txtFuelPrice.text.toString().toBigDecimal()
        val cost = txtFuelCost.text.toString().toBigDecimal()

        val computedCost = volume.multiply(price)
        val costToleranceUpper = computedCost.add(BigDecimal.valueOf(0.03))
        val costToleranceLower = computedCost.subtract(BigDecimal.valueOf(0.03))
        if (!MathHelper.inBetween(cost, costToleranceLower, costToleranceUpper)) {
            txtFuelCost.error = "Volume and Price computation do not match with Cost"
            return false
        }

        return true
    }

    private fun save() {
        val fuel = fuel
        if (this.fuel == null) {
            //Add a fuel
            val vehicleId = vehicleId
            if (vehicleId == null) {
                Toast.makeText(activity, "Failed to save fuel", Toast.LENGTH_LONG).show()
                return
            }

            val newFuel = Fuel(fuel?._id,
                vehicleId,
                Fuel.convertDateFromUserInput(txtFuelDateTime.text.toString()),
                Integer.parseInt(txtFuelOdometer.text.toString()),
                txtFuelVolume.text.toString().toBigDecimal(),
                txtFuelPrice.text.toString().toBigDecimal(),
                txtFuelCost.text.toString().toBigDecimal(),
                swFuelIsFull.isChecked,
                swFuelIsMissed.isChecked,
                swFuelIsOdoEst.isChecked)

            listener?.onFuelMaintainSave(newFuel, true)

        } else {
            //Edit a fuel
            if (fuel == null) {
                Toast.makeText(activity, "No fuel object found to edit", Toast.LENGTH_LONG).show()
                return
            }

            val newFuel = Fuel(fuel._id,
                fuel.vehicle,
                null,
                Integer.parseInt(txtFuelOdometer.text.toString()),
                txtFuelVolume.text.toString().toBigDecimal(),
                txtFuelPrice.text.toString().toBigDecimal(),
                txtFuelCost.text.toString().toBigDecimal(),
                swFuelIsFull.isChecked,
                swFuelIsMissed.isChecked,
                swFuelIsOdoEst.isChecked,
                fuel.mileage,
                fuel.pricekm)

            if (this.fuel == newFuel) {
                Toast.makeText(activity, "There are no changes", Toast.LENGTH_LONG).show()
                return
            }
            listener?.onFuelMaintainSave(newFuel, false)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param fuel Fuel.
         * @param token Token.
         * @return A new instance of fragment FuelMaintainFragment.
         */
        @JvmStatic
        fun newInstance(fuel: Parcelable?, vehicleId: String?, token: String?) =
            FuelMaintainFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_FUEL, fuel)
                    putString(ARG_VEHICLE_ID, vehicleId)
                    putString(ARG_TOKEN, token)
                }
            }
    }
}
