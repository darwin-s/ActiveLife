package com.darwins.activelife.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RoadsApi {
    private val BASE_URL = "https://roads.googleapis.com/"

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}