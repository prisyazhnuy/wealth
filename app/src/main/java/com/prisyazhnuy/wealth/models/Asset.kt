package com.prisyazhnuy.wealth.models

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime


@Parcelize
data class Asset(
    @JsonProperty("assetType")
    val type: String? = null,
    @JsonProperty("assetDescription")
    val description: String? = null,
    @JsonProperty("currency")
    val currency: String? = null,
    @JsonProperty("startingDate")
    val startingDate: DateTime? = null,
    @JsonProperty("category")
    val category: String? = null,
    @JsonProperty("historicalValuations")
    val historicalValuations: List<AssetValuation>? = null,
    @JsonProperty("currentValuation")
    val currentValuation: AssetValuation? = null
) : Parcelable