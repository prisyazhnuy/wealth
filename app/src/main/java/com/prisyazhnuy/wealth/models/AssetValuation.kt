package com.prisyazhnuy.wealth.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime
import java.math.BigDecimal

@Parcelize
data class AssetValuation(
        val valuationDate: DateTime? = null,
        val valuationInGBP: BigDecimal? = null,
        val valuationInCurrency: BigDecimal? = null
) : Parcelable