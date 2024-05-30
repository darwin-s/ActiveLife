package com.darwins.activelife.dto

data class User(val totalDistance: Int = 0, val following: List<String> = emptyList())
