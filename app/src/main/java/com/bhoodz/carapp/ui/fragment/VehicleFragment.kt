package com.bhoodz.carapp.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bhoodz.carapp.R
import com.bhoodz.carapp.helpers.AuthHelper
import com.bhoodz.carapp.ui.VehicleItemClickListener
import com.bhoodz.carapp.models.Vehicle
import com.bhoodz.carapp.ui.adapter.VehicleAdapter
import com.bhoodz.carapp.viewmodels.AuthViewModel
import com.bhoodz.carapp.viewmodels.SelectedItemViewModel
import com.bhoodz.carapp.viewmodels.VehicleViewModel
import kotlinx.android.synthetic.main.fragment_vehicle.*

//private const val TAG = "VehicleFragment"

/**
 * A simple [Fragment] subclass.
 * Use the [VehicleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VehicleFragment : Fragment(),
    VehicleItemClickListener.OnVehicleItemClickListener,
    VehicleAdapter.OnVehicleItemLongClickListener {

    interface OnNavigateToFuel {
        fun onNavigateToFuel(vehicle: Vehicle, token: String?)
    }

    private val vehicleAdapter = VehicleAdapter(ArrayList(), this, this)
    private val vehicleViewModel by lazy { ViewModelProviders.of(activity!!).get(VehicleViewModel::class.java) }
    private val authViewModel by lazy { ViewModelProviders.of(activity!!).get(AuthViewModel::class.java) }
    private val selectedItemViewModel by lazy { ViewModelProviders.of(activity!!).get(
        SelectedItemViewModel::class.java) }

    private var token: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vehicle, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val  vehicleItemClickListener =
            VehicleItemClickListener(
                context!!,
                lvVehicle,
                this
            )
        lvVehicle.layoutManager = LinearLayoutManager(context)
        lvVehicle.addOnItemTouchListener(vehicleItemClickListener)
        lvVehicle.adapter = vehicleAdapter

        vehicleViewModel.vehicleEntries.observe(viewLifecycleOwner, Observer { vehicleEntries ->
            if (vehicleEntries != null) {
                vehicleAdapter.loadData(vehicleEntries)
            }
        })

        vehicleViewModel.errorMessageEntry.observe(viewLifecycleOwner, Observer { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(activity, error, Toast.LENGTH_LONG).show()
            }
        })

        authViewModel.tokenEntry.observe(viewLifecycleOwner, Observer { token ->
            this.token = token
            if (!AuthHelper.isTokenExpired(token)) {
                vehicleViewModel.getVehicles(token)
            }
        })

        selectedItemViewModel.selectedVehicleEntry.observe(viewLifecycleOwner, Observer { selectedItem ->
            if (selectedItem == null) {
                vehicleAdapter.clearSelected()
            } else {
                vehicleAdapter.updateSelected(selectedItem.rowIndex)
            }
        })
    }

    override fun onVehicleItemClick(view: View, position: Int) {
        val vehicle = vehicleAdapter.getVehicle(position)
        if (vehicle != null) {
//            val intent = Intent(context, FuelActivity::class.java)
//            intent.putExtra(VEHICLE_DATA, vehicle)
//            startActivity(intent)
            (activity as OnNavigateToFuel).onNavigateToFuel(vehicle, token)
        }
    }

    override fun onVehicleItemSelected(position: Int, vehicle: Vehicle) {
        selectedItemViewModel.setSelectedVehicle(position, vehicle)
    }

    override fun onVehicleItemDeselected() {
        selectedItemViewModel.clearSelectedVehicle()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment VehicleFragment.
         */
        @JvmStatic
        fun newInstance() = VehicleFragment()
    }
}
