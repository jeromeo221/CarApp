package com.bhoodz.carapp.helpers

import java.math.BigDecimal

object MathHelper {
    fun inBetween(value: BigDecimal, lowerLimit: BigDecimal, upperLimit: BigDecimal): Boolean {
        return value > lowerLimit && value < upperLimit;
    }
}