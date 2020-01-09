package com.example.justeat.Common

import com.example.myapplication.Model.Results
import com.example.myapplication.Remote.RetrofitClient
import com.example.myapplication.Remote.iGoogleAPIService
import java.util.ArrayList


object Common {
    private val GOOGLE_API_URL="https://maps.googleapis.com/"

    var currentResult: Results?=null
    val googleAPIService:iGoogleAPIService
        get() = RetrofitClient.getClient(GOOGLE_API_URL).create(iGoogleAPIService::class.java)
}