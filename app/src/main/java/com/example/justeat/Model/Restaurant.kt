package com.example.justeathj.model

import com.example.myapplication.Model.OpeningHours

data class Restaurant(
    var place_id:String,
    var name: String,
    var address: String,
    var distanceInKm: String,
    var isOpenNow: Boolean,
    var priceLevel: Int,
    var rating: Double,
    var userRatingsTotal: Int
)
