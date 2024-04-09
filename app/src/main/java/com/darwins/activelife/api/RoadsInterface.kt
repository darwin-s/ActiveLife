package com.darwins.activelife.api

import retrofit2.http.GET
import retrofit2.Call
import retrofit2.http.Query

interface RoadsInterface {
    @GET("/v1/nearestRoads")
    fun nearestRoad(@Query("points") points: String, @Query("key") apiKey: String): Call<Road>
}