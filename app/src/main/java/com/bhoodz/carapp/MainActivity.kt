package com.bhoodz.carapp

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bhoodz.carapp.helpers.AuthHelper
import com.bhoodz.carapp.models.Fuel
import com.bhoodz.carapp.models.SelectedItem
import com.bhoodz.carapp.models.Vehicle
import com.bhoodz.carapp.models.request.ResendRequestType
import com.bhoodz.carapp.ui.dialog.DIALOG_DATA
import com.bhoodz.carapp.ui.dialog.DIALOG_MESSAGE
import com.bhoodz.carapp.ui.dialog.DeleteDialog
import com.bhoodz.carapp.ui.dialog.LoadingDialog
import com.bhoodz.carapp.ui.fragment.*
import com.bhoodz.carapp.viewmodels.AuthViewModel
import com.bhoodz.carapp.viewmodels.FuelViewModel
import com.bhoodz.carapp.viewmodels.SelectedItemViewModel
import com.bhoodz.carapp.viewmodels.VehicleViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

private const val TAG = "MainActivity"
private const val FRAGMENT_FUEL = "FRAGMENT_FUEL"

class MainActivity : AppCompatActivity(),
    VehicleFragment.OnNavigateToFuel,
    VehicleMaintainFragment.OnVehicleMaintainSave,
    FuelMaintainFragment.OnFuelMaintainSave,
    DeleteDialog.DeleteDialogEvent {

    private val vehicleViewModel by lazy { ViewModelProviders.of(this).get(VehicleViewModel::class.java) }
    private val authViewModel by lazy { ViewModelProviders.of(this).get(AuthViewModel::class.java) }
    private val fuelViewModel by lazy { ViewModelProviders.of(this).get(FuelViewModel::class.java) }
    private val selectedItemViewModel by lazy { ViewModelProviders.of(this).get(SelectedItemViewModel::class.java) }
    private var mobileToken: String? = null
    private var token: String? = null
    private var selectedVehicleItem: SelectedItem<Vehicle>? = null
    private var selectedFuelItem: SelectedItem<Fuel>? = null
    private var currentVehicleId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val authLoadingDialog = LoadingDialog(this)
        val objectLoadingDialog = LoadingDialog(this)

        authViewModel.isLoadingEntry.observe(this, Observer { isLoading ->
            processLoadingDialog(authLoadingDialog, isLoading)
        })

        authViewModel.mobileTokenEntry.observe(this, Observer { token ->
            invalidateOptionsMenu()
            mobileToken = token
            if (token == null) {
                showLoginScreen()
            } else {
                //No need for password as it will use the mobile token
                authViewModel.login()
                hideLoginScreen()
            }
        })

        authViewModel.tokenEntry.observe(this, Observer { token ->
            this.token = token
        })

        authViewModel.resendRequestEntry.observe(this, Observer {resendRequest ->
            if (resendRequest != null) {
                Log.d(TAG, "CALLING RESEND REQUEST: $resendRequest")
                when (resendRequest.type) {
                    ResendRequestType.GET_VEHICLES -> {
                        vehicleViewModel.getVehicles(token)
                    }
                    ResendRequestType.ADD_VEHICLE -> {
                        vehicleViewModel.addVehicle(resendRequest.entity as Vehicle, token)
                    }
                    ResendRequestType.UPDATE_VEHICLE -> {
                        vehicleViewModel.updateVehicle(resendRequest.entity as Vehicle, token)
                    }
                    ResendRequestType.DELETE_VEHICLE -> {
                        val entity = resendRequest.entity as Vehicle
                        vehicleViewModel.deleteVehicle(entity._id!!, token)
                    }
                    ResendRequestType.GET_FUELS -> {
                        val entity = resendRequest.entity as Vehicle
                        fuelViewModel.getFuels(entity._id, resendRequest.token)
                    }
                    ResendRequestType.ADD_FUEL -> {
                        fuelViewModel.addFuel(resendRequest.entity as Fuel, resendRequest.token)
                    }
                    ResendRequestType.UPDATE_FUEL -> {
                        fuelViewModel.updateFuel(resendRequest.entity as Fuel, token)
                    }
                    ResendRequestType.DELETE_FUEL -> {
                        fuelViewModel.deleteFuel(resendRequest.entity as Fuel, token)
                    }
                }
            }
        })

        authViewModel.errorMessageEntry.observe(this, Observer { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        })

        vehicleViewModel.isLoadingMaintainEntry.observe(this, Observer { isLoading ->
            processLoadingDialog(objectLoadingDialog, isLoading)
        })

        vehicleViewModel.isSuccessMaintainEntry.observe(this, Observer { isSuccess ->
            if (isSuccess) {
                supportFragmentManager.popBackStack()
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                supportActionBar?.title = getString(R.string.app_name)
                fab.show()
                vehicleFragment.visibility = View.VISIBLE
                invalidateOptionsMenu()
            }
        })

        fuelViewModel.isLoadingMaintainEntry.observe(this, Observer { isLoading ->
            processLoadingDialog(objectLoadingDialog, isLoading)
        })

        fuelViewModel.vehicleIdEntry.observe(this, Observer { vehicleId ->
            Log.d(TAG, "current vehicle id: $vehicleId")
            currentVehicleId = vehicleId
        })

        fuelViewModel.isSuccessMaintainEntry.observe(this, Observer { isSuccess ->
            if (isSuccess) {
                supportFragmentManager.popBackStack()
                fab.show()
                invalidateOptionsMenu()
            }
        })

        selectedItemViewModel.selectedVehicleEntry.observe(this, Observer { selectedItem ->
            this.selectedVehicleItem = selectedItem
            invalidateOptionsMenu()
        })

        selectedItemViewModel.selectedFuelEntry.observe(this, Observer { selectedItem ->
            this.selectedFuelItem = selectedItem
            invalidateOptionsMenu()
        })

        fab.setOnClickListener {
            when (supportFragmentManager.findFragmentById(R.id.main_container)) {
                null -> {
                    //We are at vehicle page
                    val vehicleMaintainFragment = VehicleMaintainFragment.newInstance(null, token)
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, vehicleMaintainFragment).addToBackStack(null).commit()
                }
                is FuelFragment -> {
                    //We are at fuel page
                    val fuelMaintainFragment = FuelMaintainFragment.newInstance(null, currentVehicleId, token)
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, fuelMaintainFragment).addToBackStack(null).commit()
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        invalidateOptionsMenu()
        when (supportFragmentManager.findFragmentById(R.id.main_container)) {
            null -> {
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                supportActionBar?.title = getString(R.string.app_name)
                vehicleFragment.visibility = View.VISIBLE
                fuelViewModel.setVehicleId(null)
                fab.show()
            }
            is FuelFragment -> {
                supportActionBar?.title = getString(R.string.title_fragment_fuel)
                fab.show()
            }
        }
        hideKeyboard()
    }

    private fun processLoadingDialog(dialog: LoadingDialog, isLoading: Boolean) {
        if (isLoading) {
            dialog.show()
        } else {
            dialog.dismiss()
        }
    }

    override fun onNavigateToFuel(vehicle: Vehicle, token: String?) {
        fuelViewModel.clearFuels()
        val fuelFragment = FuelFragment.newInstance(vehicle, token)
        supportFragmentManager.beginTransaction().add(R.id.main_container, fuelFragment, FRAGMENT_FUEL).addToBackStack(null).commit()
    }

    override fun onVehicleMaintainSave(vehicle: Vehicle, isAdd: Boolean) {
        Log.d(TAG, "Saving Vehicle: $vehicle")
        selectedItemViewModel.clearSelectedVehicle()

        if (isAdd) {
            if (AuthHelper.isTokenExpired(token)) {
                authViewModel.relogin(ResendRequestType.ADD_VEHICLE, vehicle)
            } else {
                vehicleViewModel.addVehicle(vehicle, token)
            }

        } else {
            if (AuthHelper.isTokenExpired(token)) {
                authViewModel.relogin(ResendRequestType.UPDATE_VEHICLE, vehicle)
            } else {
                vehicleViewModel.updateVehicle(vehicle, token)
            }
        }
        hideKeyboard()
    }

    override fun onFuelMaintainSave(fuel: Fuel, isAdd: Boolean) {
        Log.d(TAG, "Saving Fuel: $fuel")
        selectedItemViewModel.clearSelectedFuel()

        if (isAdd) {
            if (AuthHelper.isTokenExpired(token)) {
                authViewModel.relogin(ResendRequestType.ADD_FUEL, fuel)
            } else {
                fuelViewModel.addFuel(fuel, token)
            }

        } else {
            if (AuthHelper.isTokenExpired(token)) {
                authViewModel.relogin(ResendRequestType.UPDATE_FUEL, fuel)
            } else {
                fuelViewModel.updateFuel(fuel, token)
            }
        }

        hideKeyboard()
    }

    override fun onDeleteConfirm(data: Parcelable) {
        when (data) {
            is Vehicle -> {
                val id = data._id
                if (id != null) {
                    if (AuthHelper.isTokenExpired(token)) {
                        authViewModel.relogin(ResendRequestType.DELETE_VEHICLE, data)
                    } else {
                        vehicleViewModel.deleteVehicle(id, token)
                    }

                }
                selectedItemViewModel.clearSelectedVehicle()
            }
            is Fuel -> {
                if (AuthHelper.isTokenExpired(token)) {
                    authViewModel.relogin(ResendRequestType.DELETE_FUEL, data)
                } else {
                    fuelViewModel.deleteFuel(data, token)
                }
                selectedItemViewModel.clearSelectedFuel()
            }
        }
    }

    private fun showLoginScreen() {
        val newLoginFragment = LoginFragment.newInstance()
        supportFragmentManager.beginTransaction().replace(R.id.login_container, newLoginFragment).commit()
        login_container.visibility = View.VISIBLE
        vehicleFragment.visibility = View.GONE
        fab.hide()
    }

    private fun hideLoginScreen() {
        login_container.visibility = View.GONE
        vehicleFragment.visibility = View.VISIBLE
        fab.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        //Derive if Logout menu will be shown
        val logoutMenu = menu.findItem(R.id.menuLogout)
        val fragment = supportFragmentManager.findFragmentById(R.id.main_container)

        logoutMenu.isVisible = mobileToken != null && fragment == null

        //Derive if maintain menu group will be shown
        val maintainEdit = menu.findItem(R.id.menuEditItem)
        val maintainDelete = menu.findItem(R.id.menuDeleteItem)

        var isMaintainVisible = false
        if (fragment !is MaintainFragment) {
            if (selectedVehicleItem != null && fragment == null) {
                isMaintainVisible = true
            }
            val selectedFuelItem = selectedFuelItem
            if (selectedFuelItem != null
                && fragment is FuelFragment
                && selectedFuelItem.entity.vehicle == currentVehicleId) {

                isMaintainVisible = true
            }
        }

        maintainEdit.isVisible = isMaintainVisible
        maintainDelete.isVisible = isMaintainVisible
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.menuLogout -> {
                authViewModel.logout()
                selectedItemViewModel.clearSelectedVehicle()
                selectedItemViewModel.clearSelectedFuel()
                false
            }
            android.R.id.home -> {
                onBackPressed()
                false
            }
            R.id.menuEditItem -> {
                when (supportFragmentManager.findFragmentById(R.id.main_container)) {
                    null -> {
                        val vehicleMaintainFragment = VehicleMaintainFragment.newInstance(selectedVehicleItem?.entity, token)
                        supportFragmentManager.beginTransaction().replace(R.id.main_container, vehicleMaintainFragment).addToBackStack(null).commit()
                    }
                    is FuelFragment -> {
                        val fuel = selectedFuelItem?.entity
                        if (fuel != null) {
                            val fuelMaintainFragment = FuelMaintainFragment.newInstance(fuel, fuel.vehicle, token)
                            supportFragmentManager.beginTransaction().replace(R.id.main_container, fuelMaintainFragment).addToBackStack(null).commit()
                        }
                    }
                }
                false
            }
            R.id.menuDeleteItem -> {
                when (supportFragmentManager.findFragmentById(R.id.main_container)) {
                    null -> {
                        val args = Bundle().apply {
                            putString(DIALOG_MESSAGE, "Are you sure you want to delete this vehicle?")
                            putParcelable(DIALOG_DATA, selectedVehicleItem?.entity)
                        }
                        val dialog = DeleteDialog()
                        dialog.arguments = args
                        dialog.show(supportFragmentManager, null)
                    }
                    is FuelFragment -> {
                        val args = Bundle().apply {
                            putString(DIALOG_MESSAGE, "Are you sure you want to delete this fuel transaction?")
                            putParcelable(DIALOG_DATA, selectedFuelItem?.entity)
                        }
                        val dialog = DeleteDialog()
                        dialog.arguments = args
                        dialog.show(supportFragmentManager, null)
                    }
                }
                false
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun hideKeyboard() {
        val currentFocus = currentFocus
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }
}
