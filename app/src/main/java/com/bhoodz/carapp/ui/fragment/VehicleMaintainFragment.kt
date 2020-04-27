package com.bhoodz.carapp.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.auth0.android.jwt.JWT
import com.bhoodz.carapp.R
import com.bhoodz.carapp.helpers.AuthHelper
import com.bhoodz.carapp.models.TokenPayload
import com.bhoodz.carapp.models.Vehicle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_vehicle_maintain.*


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_VEHICLE = "vehicle"
private const val ARG_TOKEN = "token"
//private const val TAG = "VehicleMaintFrag"

/**
 * A simple [Fragment] subclass.
 * Use the [VehicleMaintainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VehicleMaintainFragment : MaintainFragment() {
    private var vehicle: Vehicle? = null
    private var token: String? = null
    private var listener: OnVehicleMaintainSave? = null

    interface OnVehicleMaintainSave {
        fun onVehicleMaintainSave(vehicle: Vehicle, isAdd: Boolean)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnVehicleMaintainSave) {
            listener = context
        } else {
            throw RuntimeException("$context must implement onVehicleMaintainSave")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            vehicle = it.getParcelable(ARG_VEHICLE)
            token = it.getString(ARG_TOKEN)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vehicle_maintain, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val vehicle = vehicle
        if (vehicle != null) {
            //Editing a vehicle
            txtVehicleMake.setText(vehicle.make)
            txtVehicleModel.setText(vehicle.model)
            txtVehicleYear.setText(vehicle.year.toString())
            txtVehicleName.setText(vehicle.name)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        with (activity as AppCompatActivity) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = if (vehicle == null) {
                getString(R.string.title_fragment_vehicle_add)
            } else {
                getString(R.string.title_fragment_vehicle_edit)
            }
            invalidateOptionsMenu()
            fab.hide()
            vehicleFragment.view?.visibility = View.INVISIBLE
        }

        btnVehicleSave.setOnClickListener {
            if (validate()) {
                save()
            }
        }
    }

    private fun validate(): Boolean {
        if (txtVehicleMake.text.isEmpty()) {
            txtVehicleMake.error = "Vehicle Make is required"
            return false
        }
        if (txtVehicleModel.text.isEmpty()) {
            txtVehicleModel.error = "Vehicle Model is required"
            return false
        }
        if (txtVehicleYear.text.isEmpty()) {
            txtVehicleYear.error = "Vehicle Year is required"
            return false
        }
        return true
    }

    private fun save() {
        val token = token
        if (token == null) {
            Toast.makeText(activity, "Unauthorized to save a vehicle", Toast.LENGTH_LONG).show()
            return
        }

        val tokenPayload = AuthHelper.getTokenPayload(token)
        if (tokenPayload == null) {
            Toast.makeText(activity, "Unauthorized to save a vehicle", Toast.LENGTH_LONG).show()
            return
        }

        val vehicle = Vehicle(this.vehicle?._id,
            txtVehicleMake.text.toString(),
            txtVehicleModel.text.toString(),
            Integer.parseInt(txtVehicleYear.text.toString()),
            tokenPayload.userId,
            txtVehicleName.text.toString())

        if (this.vehicle == null) {
            //Add the vehicle
            listener?.onVehicleMaintainSave(vehicle, true)
        } else {
            //Edit the vehicle
            if (this.vehicle == vehicle) {
                Toast.makeText(activity, "There are no changes", Toast.LENGTH_LONG).show()
                return
            }
            listener?.onVehicleMaintainSave(vehicle, false)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param vehicle Vehicle.
         * @param token Token.
         * @return A new instance of fragment VehicleMaintainFragment.
         */
        @JvmStatic
        fun newInstance(vehicle: Vehicle?, token: String?) =
            VehicleMaintainFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_VEHICLE, vehicle)
                    putString(ARG_TOKEN, token)
                }
            }
    }
}
