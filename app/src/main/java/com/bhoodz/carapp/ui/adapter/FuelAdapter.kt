package com.bhoodz.carapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bhoodz.carapp.R
import com.bhoodz.carapp.models.Fuel
import java.math.BigDecimal
import java.util.*

class FuelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvFuelDate: TextView = view.findViewById(R.id.tvFuelDate)
    val tvFuelOdometer: TextView = view.findViewById(R.id.tvFuelOdometer)
    val tvFuelPrice: TextView = view.findViewById(R.id.tvFuelPrice)
    val tvFuelVolume: TextView = view.findViewById(R.id.tvFuelVolume)
    val tvFuelCost: TextView = view.findViewById(R.id.tvFuelCost)
    val tvFuelMileage: TextView = view.findViewById(R.id.tvFuelMileage)
    val lblFuelMileage: TextView = view.findViewById(R.id.lblFuelMileage)
    val tvFuelPriceKm: TextView = view.findViewById(R.id.tvFuelPriceKm)
    val lblFuelPriceKm: TextView = view.findViewById(R.id.lblFuelPriceKm)
    val tvFuelFull: TextView = view.findViewById(R.id.tvFuelFull)
    val tvFuelMissed: TextView = view.findViewById(R.id.tvFuelMissed)
    val tvFuelEstOdo: TextView = view.findViewById(R.id.tvFuelEstOdo)
}

class FuelAdapter(private var fuelList: List<Fuel>,
                  private val listener: OnFuelItemLongClickListener,
                  private val fragment: Fragment)
    : RecyclerView.Adapter<FuelViewHolder>() {

    interface OnFuelItemLongClickListener {
        fun onFuelItemSelected(position: Int, fuel: Fuel)
        fun onFuelItemDeselected()
    }

    private var rowSelected = -1
    private var vehicleIdSelected: String? = null

    fun loadData(data: List<Fuel>) {
        this.fuelList = data
        notifyDataSetChanged()
    }

    fun clearSelected() {
        rowSelected = -1
        vehicleIdSelected = null
        notifyDataSetChanged()
    }

    fun updateSelected(rowIndex: Int, vehicleId: String) {
        rowSelected = rowIndex
        vehicleIdSelected = vehicleId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FuelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fuel_item, parent, false)
        return FuelViewHolder(view)
    }

    override fun onBindViewHolder(holder: FuelViewHolder, position: Int) {
        if (fuelList.isEmpty()) {
            holder.tvFuelDate.text = ""
            holder.tvFuelOdometer.text = ""
            holder.tvFuelPrice.text = ""
            holder.tvFuelVolume.text = ""
            holder.tvFuelCost.text = ""
            holder.tvFuelMileage.text = ""
            holder.tvFuelPriceKm.text = ""
            holder.lblFuelMileage.visibility = View.VISIBLE
            holder.lblFuelPriceKm.visibility = View.VISIBLE
            holder.tvFuelFull.visibility = View.INVISIBLE
            holder.tvFuelMissed.visibility = View.INVISIBLE
            holder.tvFuelEstOdo.visibility = View.INVISIBLE

        } else {
            val fuel: Fuel = fuelList[position]
            val mileage = displayComputation(fuel.mileage?.setScale(2, BigDecimal.ROUND_HALF_DOWN))
            val priceKm = displayComputation(fuel.pricekm?.setScale(2, BigDecimal.ROUND_HALF_DOWN))
            holder.tvFuelDate.text = Fuel.prepareDateToDisplay(fuel.date!!)
            holder.tvFuelOdometer.text = fuel.odometer.toString()
            with (fragment.requireContext()) {
                holder.tvFuelPrice.text = getString(R.string.fuel_price, String.format(Locale.getDefault(), fuel.price.setScale(2, BigDecimal.ROUND_HALF_DOWN).toString()))
                holder.tvFuelVolume.text = getString(R.string.fuel_volume, String.format(Locale.getDefault(), fuel.volume.setScale(2, BigDecimal.ROUND_HALF_DOWN).toString()))
                holder.tvFuelCost.text = getString(R.string.fuel_cost, String.format(Locale.getDefault(), fuel.cost.setScale(2, BigDecimal.ROUND_HALF_DOWN).toString()))
                holder.tvFuelMileage.text = if (mileage.isNotEmpty()) getString(R.string.fuel_mileage, String.format(Locale.getDefault(), mileage)) else ""
                holder.tvFuelPriceKm.text = if (priceKm.isNotEmpty()) getString(R.string.fuel_priceKm, String.format(Locale.getDefault(), priceKm)) else ""
            }
            holder.lblFuelMileage.visibility = deriveVisibility(mileage)
            holder.lblFuelPriceKm.visibility = deriveVisibility(priceKm)
            holder.tvFuelFull.visibility = deriveVisibility(fuel.isFull)
            holder.tvFuelMissed.visibility = deriveVisibility(fuel.isMissed)
            holder.tvFuelEstOdo.visibility = deriveVisibility(fuel.isEstOdo)

            if (rowSelected == position && vehicleIdSelected == fuel.vehicle) {
                holder.itemView.setBackgroundColor(fragment.requireContext().getColor(R.color.colorAccent))
            } else {
                holder.itemView.setBackgroundColor(fragment.requireContext().getColor(R.color.listBackground))
            }
        }

        holder.itemView.setOnLongClickListener {
            if (fuelList.isNotEmpty()) {
                if (rowSelected == position) {
                    //Deselect vehicle
                    rowSelected = -1
                    notifyDataSetChanged()
                    listener.onFuelItemDeselected()

                } else {
                    //Select the vehicle
                    rowSelected = position
                    notifyDataSetChanged()
                    listener.onFuelItemSelected(position, fuelList[position])
                }
            }
            true
        }
    }

    override fun getItemCount(): Int {
        return if (fuelList.isEmpty()) {
            1
        } else {
            fuelList.size
        }
    }

    private fun deriveVisibility(text: String?): Int {
        return if (text != null && text.isNotEmpty()) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }

    private fun deriveVisibility(flag: Boolean): Int {
        return if (flag) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }

    private fun displayComputation(value: BigDecimal?): String {
        return value?.toString() ?: ""
    }
}