package com.example.justeathj.model

import com.example.myapplication.Model.OpeningHours

data class Restaurant(
    var placeId:String,
    var name: String,
    var address: String,
    var isOpenNow: Boolean,
    var priceLevel: Int,
    var rating: Double,
    var userRatingsTotal: Int
)
