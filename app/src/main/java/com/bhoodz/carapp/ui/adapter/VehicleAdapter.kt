package com.bhoodz.carapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bhoodz.carapp.R
import com.bhoodz.carapp.models.Vehicle

//private const val TAG = "VehicleAdapter"

class VehicleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvVehicleTitle: TextView = view.findViewById(R.id.tvVehicleTitle)
    val tvVehicleDescription: TextView = view.findViewById(R.id.tvVehicleDescription)
}

class VehicleAdapter(private var vehicleList: List<Vehicle>,
                     private val listener: OnVehicleItemLongClickListener,
                     private val fragment: Fragment)
    : RecyclerView.Adapter<VehicleViewHolder>() {

    interface OnVehicleItemLongClickListener {
        fun onVehicleItemSelected(position: Int, vehicle: Vehicle)
        fun onVehicleItemDeselected()
    }

    private var rowSelected = -1

    fun loadData(data: List<Vehicle>){
        this.vehicleList = data
        notifyDataSetChanged()
    }

    fun clearSelected() {
        rowSelected = -1
        notifyDataSetChanged()
    }

    fun updateSelected(rowIndex: Int) {
        rowSelected = rowIndex
        notifyDataSetChanged()
    }

    fun getVehicle(position: Int): Vehicle? {
        return if (vehicleList.isNotEmpty()) vehicleList[position] else null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.vehicle_item, parent, false)
        return VehicleViewHolder(view)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        if (vehicleList.isEmpty()) {
            holder.tvVehicleTitle.text = fragment.getString(R.string.vehicle_no_available)
        } else {
            val vehicle: Vehicle = vehicleList[position]
            //holder.tvVehicleTitle.text = "${vehicle.make} ${vehicle.model} ${vehicle.year}"
            holder.tvVehicleTitle.text = fragment.getString(R.string.vehicle_title, vehicle.make, vehicle.model, vehicle.year)
            holder.tvVehicleDescription.text = vehicle.name
        }

        if (rowSelected == position) {
            holder.itemView.setBackgroundColor(fragment.requireContext().getColor(R.color.colorAccent))
        } else {
            holder.itemView.setBackgroundColor(fragment.requireContext().getColor(R.color.cardBackground))
        }

        holder.itemView.setOnLongClickListener {
            if (vehicleList.isNotEmpty()) {
                if (rowSelected == position) {
                    //Deselect vehicle
                    rowSelected = -1
                    notifyDataSetChanged()
                    listener.onVehicleItemDeselected()
                } else {
                    //Select the vehicle
                    rowSelected = position
                    notifyDataSetChanged()
                    listener.onVehicleItemSelected(position, vehicleList[position])
                }
            }
            true
        }
    }

    override fun getItemCount(): Int {
        return if (vehicleList.isEmpty()) {
            1
        } else {
            vehicleList.size
        }
    }
}