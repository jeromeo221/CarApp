package com.bhoodz.carapp.models

import android.os.Parcelable

data class SelectedItem<T>(val rowIndex: Int, val entity: T)