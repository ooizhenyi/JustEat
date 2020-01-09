

package com.example.myapplication.Remote

import DistanceM
import com.example.myapplication.Model.MyPlaces
import com.example.myapplication.Model.PlaceDetail
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface iGoogleAPIService {
    @GET
    fun getNearbyPlace(@Url url: String): Call<MyPlaces>

    @GET
    fun getDetailPlace(@Url url: String): Call<PlaceDetail>

    @GET
    fun getDistanceMatrix(@Url url: String): Call<DistanceM>

}