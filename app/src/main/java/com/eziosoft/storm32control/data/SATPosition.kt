package com.eziosoft.storm32control.data


import com.google.gson.annotations.SerializedName

data class SATPosition(
    @SerializedName("info")
    val info: Info,
    @SerializedName("positions")
    val positions: List<Position>
) {
    data class Info(
        @SerializedName("satid")
        val satid: Int,
        @SerializedName("satname")
        val satname: String,
        @SerializedName("transactionscount")
        val transactionscount: Int
    )

    data class Position(
        @SerializedName("azimuth")
        val azimuth: Double,
        @SerializedName("dec")
        val dec: Double,
        @SerializedName("elevation")
        val elevation: Double,
        @SerializedName("ra")
        val ra: Double,
        @SerializedName("sataltitude")
        val sataltitude: Double,
        @SerializedName("satlatitude")
        val satlatitude: Double,
        @SerializedName("satlongitude")
        val satlongitude: Double,
        @SerializedName("timestamp")
        val timestamp: Int
    )
}