package com.bhoodz.carapp.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bhoodz.carapp.R
import com.bhoodz.carapp.helpers.AuthHelper
import com.bhoodz.carapp.models.Fuel
import com.bhoodz.carapp.models.Vehicle
import com.bhoodz.carapp.models.request.ResendRequestType
import com.bhoodz.carapp.ui.adapter.FuelAdapter
import com.bhoodz.carapp.viewmodels.AuthViewModel
import com.bhoodz.carapp.viewmodels.FuelViewModel
import com.bhoodz.carapp.viewmodels.SelectedItemViewModel
import kotlinx.android.synthetic.main.fragment_fuel.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_VEHICLE = "vehicle"
private const val ARG_TOKEN = "token"
private const val TAG = "FuelFragment"

/**
 * A simple [Fragment] subclass.
 * Use the [FuelFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FuelFragment : Fragment(),
    FuelAdapter.OnFuelItemLongClickListener {

    private var vehicle: Vehicle? = null
    private var token: String? = null
    private val fuelViewModel by lazy { ViewModelProviders.of(requireActivity()).get(FuelViewModel::class.java) }
    private val authViewModel by lazy { ViewModelProviders.of(requireActivity()).get(AuthViewModel::class.java) }
    private val selectedItemViewModel by lazy { ViewModelProviders.of(requireActivity()).get(
        SelectedItemViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            vehicle = it.getParcelable(ARG_VEHICLE)
            token = it.getString(ARG_TOKEN)
        }

        getFuels()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fuel, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with (activity as AppCompatActivity) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = getString(R.string.title_fragment_fuel)
            invalidateOptionsMenu()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val fuelAdapter = FuelAdapter(ArrayList(), this, this)
        lvFuel.layoutManager = LinearLayoutManager(activity)
        lvFuel.adapter = fuelAdapter

        val vehicle = vehicle
        if (vehicle != null) {
            lblFuelVehicle.text = getString(R.string.vehicle_title, vehicle.make, vehicle.model, vehicle.year)
        }

        val swipeRefreshFuel = requireActivity().findViewById<SwipeRefreshLayout>(R.id.swipeRefreshFuel)
        swipeRefreshFuel.setOnRefreshListener {
            getFuels()
        }

        fuelViewModel.fuelEntries.observe(viewLifecycleOwner, Observer { fuelEntries ->
            fuelAdapter.loadData(fuelEntries)
        })

        fuelViewModel.errorMessageEntry.observe(viewLifecycleOwner, Observer { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(activity, error, Toast.LENGTH_LONG).show()
            }
        })

        fuelViewModel.isLoadingGetEntry.observe(viewLifecycleOwner, Observer {isLoading ->
            swipeRefreshFuel.isRefreshing = isLoading
        })

        selectedItemViewModel.selectedFuelEntry.observe(viewLifecycleOwner, Observer { selectedItem ->
            if (selectedItem == null) {
                fuelAdapter.clearSelected()
            } else {
                fuelAdapter.updateSelected(selectedItem.rowIndex, selectedItem.entity.vehicle)
            }
        })

        authViewModel.tokenEntry.observe(viewLifecycleOwner, Observer {token ->
            this.token = token
        })
    }

    override fun onFuelItemSelected(position: Int, fuel: Fuel) {
        selectedItemViewModel.setSelectedFuel(position, fuel)
    }

    override fun onFuelItemDeselected() {
        selectedItemViewModel.clearSelectedFuel()
    }

    private fun getFuels() {
        val vehicle = vehicle
        if (vehicle != null) {
            fuelViewModel.setVehicleId(vehicle._id)
            if (AuthHelper.isTokenExpired(token)) {
                authViewModel.relogin(ResendRequestType.GET_FUELS, vehicle)
            } else {
                fuelViewModel.getFuels(vehicle._id, token)
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param vehicle Vehicle.
         * @return A new instance of fragment FuelFragment.
         */
        @JvmStatic
        fun newInstance(vehicle: Vehicle, token: String?) =
            FuelFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_VEHICLE, vehicle)
                    putString(ARG_TOKEN, token)
                }
            }
    }
}
