package com.darwins.activelife.api

import com.google.gson.annotations.SerializedName

data class Road(
    @SerializedName("snappedPoints")
    val snappedPoints: Array<SnappedPoint>
)

data class SnappedPoint(
    @SerializedName("location")
    val location: Location,

    @SerializedName("placeId")
    val placeId: String,

    @SerializedName("originalIndex")
    val originalIndex: Number,
)

data class Location (
    @SerializedName("latitude")
    val latitude: Number,

    @SerializedName("longitude")
    val longitude: Number
)