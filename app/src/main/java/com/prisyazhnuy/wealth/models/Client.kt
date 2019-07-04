package com.prisyazhnuy.wealth.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Client(
    val clientName: String? = null,
    val assets: List<Asset>? = null
) : Parcelable