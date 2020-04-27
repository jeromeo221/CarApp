package com.bhoodz.carapp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_fuel_maintain.*
import java.math.BigDecimal
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

const val FUEL_DATE_DISPLAY = "yyyy-MM-dd hh:mm a"
private const val FUEL_DATE_INTERNAL = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

@Parcelize
data class Fuel(var _id: String?,
                var vehicle: String,
                val date: String?,
                val odometer: Int,
                val volume: BigDecimal,
                val price: BigDecimal,
                val cost: BigDecimal,
                val isFull: Boolean,
                val isMissed: Boolean,
                val isEstOdo: Boolean,
                val mileage: BigDecimal? = null,
                val pricekm: BigDecimal? = null) : Parcelable {

    override fun toString(): String {
        return "Vehicle: $vehicle, Date: $date, Odometer: $odometer, Volume: $volume, Price: $price, Cost: $cost, Mileage: $mileage"
    }

    companion object {
        fun convertDateFromUserInput(dateTimeString: String): String {
            val dateTime= convertDateStringToLocalDateTime(dateTimeString, FUEL_DATE_DISPLAY)

            //Convert Local time zone to GMT time
            val localDateTime = dateTime.atZone(ZoneId.systemDefault())
            val utcDateTime = localDateTime.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()
            return convertLocalDateTimeToString(utcDateTime, FUEL_DATE_INTERNAL)
        }

        fun retrieveTodayForDisplay(): String {
            val now = LocalDateTime.now().atZone(ZoneId.systemDefault())
            return convertLocalDateTimeToString(now.toLocalDateTime(), FUEL_DATE_DISPLAY)
        }

        fun prepareDateToDisplay(dateTimeString: String): String {
            val dateTime= convertDateStringToLocalDateTime(dateTimeString, FUEL_DATE_INTERNAL)

            //Convert GMT time to local time zone
            val utcDateTime = dateTime.atZone(ZoneId.of("UTC"))
            val localDateTime = utcDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
            return convertLocalDateTimeToString(localDateTime, FUEL_DATE_DISPLAY)
        }

        fun isValidDateUserInput(dateTimeString: String): Boolean {
            try {
                convertDateStringToLocalDateTime(dateTimeString, FUEL_DATE_DISPLAY)
            } catch(e: DateTimeParseException) {
                return false
            }
            return true
        }
    }
}

private fun convertDateStringToLocalDateTime(dateTimeString: String, format: String): LocalDateTime {
    val formatter = DateTimeFormatter.ofPattern(format, Locale.getDefault())
    return LocalDateTime.parse(dateTimeString, formatter)
}

private fun convertLocalDateTimeToString(dateTime: LocalDateTime, format: String): String {
    val formatter = DateTimeFormatter.ofPattern(format, Locale.getDefault())
    return formatter.format(dateTime)
}

