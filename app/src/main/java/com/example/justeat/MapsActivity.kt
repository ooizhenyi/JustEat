package com.example.justeat

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.justeat.Common.Common
import com.example.myapplication.Model.MyPlaces
import com.example.myapplication.Remote.iGoogleAPIService
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_maps.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.StringBuilder

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    var i=0

    private lateinit var mMap: GoogleMap
    private lateinit var mGoogleApiClient: GoogleApiClient

    private var latitude:Double=0.toDouble()
    private var longtitude:Double=0.toDouble()

    private lateinit var mLastLocation:android.location.Location
    private var mMarker: Marker?=null


    //location
    lateinit var fusedLocationProviderClient:FusedLocationProviderClient
    lateinit var locationRequest:LocationRequest
    lateinit var locationCallback:LocationCallback

    companion object{
        private const val MY_PERMISSION_CODE: Int = 1000
    }

    private lateinit var mServices:iGoogleAPIService
    var currentPlace:MyPlaces?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mServices = Common.googleAPIService

        //have
        //request runtime permission
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkLocationPermission()){
                buildLocationRequest()
                buildLocationCallBack()
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,Looper.myLooper())
            }
        }else{
            buildLocationRequest()
            buildLocationCallBack()
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,Looper.myLooper())
        }

        bottom_navigation_view.setOnNavigationItemSelectedListener{item->
            when(item.itemId){
                R.id.action_market -> nearByPlace("market")
                R.id.action_restaurant -> nearByPlace("restaurant")
            }
            true
        }
    }

    private fun nearByPlace(typePlace: String){

        //clear all marker in map
        mMap.clear()
        var url = getUrl(latitude,longtitude,typePlace)

        mServices.getNearbyPlace(url)
            .enqueue(object : Callback<MyPlaces> {
                override fun onResponse(call: Call<MyPlaces>?, response: Response<MyPlaces>?){
                    currentPlace = response!!.body()!!

                    if(response!!.isSuccessful){

                        for(i in 0 until response!!.body()!!.results!!.size){

                            val markerOptions=MarkerOptions()
                            val googlePlace = response.body()!!.results!![i]
                            val lat = googlePlace.geometry!!.location!!.lat
                            val lng = googlePlace.geometry!!.location!!.lng
                            val placeName = googlePlace.name
                            val latLng = LatLng(lat,lng)

                            markerOptions.position(latLng)
                            markerOptions.title(placeName)
                            /* if(typePlace.equals("market")){
                                 markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_market))}
                             else if(typePlace.equals("restaurant")){
                                 markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_restaurant))}
                             else{
                                 markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))}*/

                            markerOptions.snippet(i.toString())

                            mMap!!.addMarker(markerOptions)
                            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(LatLng(lat, lng)))
                            mMap!!.animateCamera(CameraUpdateFactory.zoomTo(11f))

                        }


                    }
                }
                override fun onFailure(call: Call<MyPlaces>?, t:Throwable?){

                    Toast.makeText(baseContext,""+t!!.message,Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getUrl(latitude: Double, longitude: Double, typePlace: String): String {

        val googlePlaceUrl = StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
        googlePlaceUrl.append("?location=$latitude,$longitude")
        googlePlaceUrl.append("&radius=10000") //10km
        googlePlaceUrl.append("&type=$typePlace")
        googlePlaceUrl.append("&key=AIzaSyABCcNkZ4q2DH34jM_IIzsQ4m9-ury_Ph0")

        Log.d("URL_DEBUG",googlePlaceUrl.toString())
        return googlePlaceUrl.toString()

    }


    //have
    private fun buildLocationCallBack(){
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(p0: LocationResult?){
                mLastLocation = p0!!.locations.get(p0!!.locations.size-1) //get last location

                if(mMarker != null)
                {
                    mMarker!!.remove()
                }

                latitude = mLastLocation.latitude
                longtitude = mLastLocation.longitude

                val latLng = LatLng(latitude,longtitude)
                val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title("Your position")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                mMarker = mMap!!.addMarker(markerOptions)

                mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap!!.animateCamera(CameraUpdateFactory.zoomTo(11f))
            }
        }
    }

    //have
    private fun buildLocationRequest(){
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement =10f
    }


    private fun checkLocationPermission():Boolean{
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION))
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ),MY_PERMISSION_CODE)
            else
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ),MY_PERMISSION_CODE)
            return false
        }else
            return true
    }

    //have
    @RequiresApi(Build.VERSION_CODES.M)
    fun onRequestPermissionResult(requestCode: Int, permission: Array<out String>, grantResults: IntArray){
        when(requestCode)
        {
            MY_PERMISSION_CODE->{
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                        if(checkLocationPermission()){
                            buildLocationRequest()
                            buildLocationCallBack()

                            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,Looper.myLooper());

                            mMap!!.isMyLocationEnabled=true
                        }
                }else{
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //have
    override fun onStop(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        /*val sydney =LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mMap!!.isMyLocationEnabled = true
            }
        } else
            mMap!!.isMyLocationEnabled = true


        mMap!!.setOnMarkerClickListener { marker ->


            if(marker.snippet != null){

                Common.currentResult = currentPlace!!.results!![Integer.parseInt(marker.snippet)]

                //Toast.makeText(baseContext,"" + marker.snippet,Toast.LENGTH_LONG).show()
                //Toast.makeText(baseContext,"" + Common.currentResult!!.photos!![0].photo_reference,Toast.LENGTH_LONG).show()
                //startActivity(Intent(this@MapsActivity, ViewPlace::class.java))

                startActivity(Intent(baseContext, ViewPlace::class.java))
            }
            true
        }

        /*mMap!!.setOnMarkerClickListener { marker ->
            Common.currentResult = currentPlace!!.results!![Integer.parseInt(marker.snippet)]
            startActivity(Intent(this@MapsActivity, ViewPlace::class.java))
            true
        }*/


    }

}