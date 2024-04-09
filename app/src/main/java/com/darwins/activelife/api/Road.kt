package com.darwins.activelife.api

import com.google.gson.annotations.SerializedName

class Road {
    @SerializedName("snappedPoints")
    lateinit var snappedPoints: Array<SnappedPoint>

    class SnappedPoint {
        @SerializedName("location")
        lateinit var location: Location

        @SerializedName("placeId")
        lateinit var placeId: String

        @SerializedName("originalIndex")
        lateinit var originalIndex: Number

        class Location {
            @SerializedName("latitude")
            lateinit var latitude: Number

            @SerializedName("longitude")
            lateinit var longitude: Number
        }
    }
}