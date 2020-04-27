package com.bhoodz.carapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bhoodz.carapp.models.Fuel
import com.bhoodz.carapp.models.SelectedItem
import com.bhoodz.carapp.models.Vehicle

class SelectedItemViewModel : ViewModel() {

    private val selectedVehicle = MutableLiveData<SelectedItem<Vehicle>>()
    val selectedVehicleEntry: LiveData<SelectedItem<Vehicle>>
        get() = selectedVehicle

    private val selectedFuel = MutableLiveData<SelectedItem<Fuel>>()
    val selectedFuelEntry: LiveData<SelectedItem<Fuel>>
        get() = selectedFuel

    init {
        selectedVehicle.postValue(null)
        selectedFuel.postValue(null)
    }

    fun clearSelectedVehicle() {
        selectedVehicle.value = null
    }

    fun clearSelectedFuel() {
        selectedFuel.value = null
    }

    fun setSelectedVehicle(rowIndex: Int, vehicle: Vehicle) {
        selectedVehicle.value = SelectedItem<Vehicle>(rowIndex, vehicle)
    }

    fun setSelectedFuel(rowIndex: Int, fuel: Fuel) {
        selectedFuel.value = SelectedItem<Fuel>(rowIndex, fuel)
    }

}