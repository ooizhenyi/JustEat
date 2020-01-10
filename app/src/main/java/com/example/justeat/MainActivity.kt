package com.example.justeat

import DistanceM
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.multidex.MultiDex
import com.example.justeat.Common.Common
import com.example.justeathj.model.Restaurant
import com.example.myapplication.Model.MyPlaces
import com.google.android.gms.location.*
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.activity_main.*
import com.example.myapplication.Remote.iGoogleAPIService
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.restaurant.*
import kotlin.collections.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.StringBuilder


private const val PERMISSION_REQUEST = 10

class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener, OnMapReadyCallback {


    //seekbar
    var progressView: TextView? = null
    var seekBarView: SeekBar? = null
    var result:Int ?=null


    //chip grp for price range
    var minpriceLevel : Int ?= null
    var maxpriceLevel : Int ?= null

    //chip grp for rating
    var rating : Double ?= null

    // switch button
    var openNow : Boolean = false


    //user location
    val PERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    //map
    //internal lateinit var mService: iGoogleAPIService
    //var mPlace: PlaceDetail? = null
    var nearby = ArrayList<Restaurant>()
    var filter = ArrayList<Restaurant>()

    var nextPageToken : String = ""
    var  requestCount:Int = 0;
     var  API_KEY2:String = "AIzaSyBUJWk3qua7OcXUyJKcc3AvohYGI7d3Vp0";
    var   REQUEST_LIMIT:Int = 3;


    //MapsActivity
    private lateinit var mMap: GoogleMap
    private lateinit var mGoogleApiClient: GoogleApiClient

    private var latitude1: Double = 0.0
    private var longtitude1: Double = 0.0

    private lateinit var mLastLocation: android.location.Location
    private var mMarker: Marker? = null



    //location
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback

    companion object {
        private const val MY_PERMISSION_CODE: Int = 1000
        private const val API_KEY = "AIzaSyBUJWk3qua7OcXUyJKcc3AvohYGI7d3Vp0"
    }

    private lateinit var mServices: iGoogleAPIService
    var currentPlace: MyPlaces? = null
    var mDistance: DistanceM? = null

    var disKM:String?=null



    override fun onCreate(savedInstanceState: Bundle?) {

        MultiDex.install(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // place_open_hour.text=""
        // val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mPlace!!.results!!.url))
        // startActivity(mapIntent)

        //user location
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getLastLocation()

        initMaps()

        //nearByPlace("restaurant")

        //switch button
        val sw = findViewById<Switch>(R.id.switch1)
        sw?.setOnCheckedChangeListener { _, isChecked ->
            val msg = if (isChecked) "ON" else "OFF"
            if (isChecked) {
                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                println("Opening Hours " + nearby!!)
                openNow = true

            } else {
                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
            }
        }


        // Seekbar
        progressView = this.textView2
        seekBarView = this.seekBar
        seekBarView!!.setOnSeekBarChangeListener(this)

        // Set chip group checked change listener
        chipGroup2.setOnCheckedChangeListener { group, checkedId: Int ->
            // Get the checked chip instance from chip group
            val chip: Chip? = findViewById(checkedId)

            if (checkedId == R.id.chip) {
                chip?.setChipBackgroundColorResource(R.color.lightBlue)
                rating = 5.0
            } else if (checkedId == R.id.chip2) {
                chip?.setChipBackgroundColorResource(R.color.lightBlue)
                rating = 5.0
            } else if (checkedId == R.id.chip3) {
                chip?.setChipBackgroundColorResource(R.color.lightBlue)
                rating = 4.0
            } else if (checkedId == R.id.chip4) {
                chip?.setChipBackgroundColorResource(R.color.lightBlue)
                rating = 3.0
            } else if (checkedId == R.id.chip5) {
                chip?.setChipBackgroundColorResource(R.color.lightBlue)
                rating = 2.0
            } else if (checkedId == R.id.chip6) {
                chip?.setChipBackgroundColorResource(R.color.lightBlue)
                rating = 1.0
            } else {
                toast("is not checked")
            }


            // chip?.let {
            //  chip?.setChipBackgroundColorResource(R.color.lightBlue)
            // Show the checked chip text on toast message
            // toast("${it.text} checked")
            // }
        }
        //chip group

        chipGroup4.setOnCheckedChangeListener { group, checkId: Int ->

            val chip: Chip? = findViewById(checkId)
            if (checkId == R.id.chip7) {
                //chip?.setChipBackgroundColorResource(R.color.lightBlue)
                minpriceLevel = 0
                maxpriceLevel = 1
            } else if (checkId == R.id.chip8) {
                chip?.setChipBackgroundColorResource(R.color.lightBlue)
                minpriceLevel = 1
                maxpriceLevel = 2
            } else if (checkId == R.id.chip9) {
                chip?.setChipBackgroundColorResource(R.color.lightBlue)
                minpriceLevel = 2
                maxpriceLevel = 3
            }

            //chip?.let {
            //  chip?.setChipBackgroundColorResource(R.color.lightBlue)
            // }
        }

        //get filter restaurant

        val confirmButon : Button = findViewById(R.id.button)
        confirmButon.setOnClickListener {
            println("Run Button")
            nearByPlace()
        }


    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.confirm -> {
            // User chose the "Settings" item, show the app settings UI...

//            mService.getDetailPlace(
//                confirmation(
//                    Common.currentResult!!.opening_hours!!.open_now.toString().plus(
//                        Common.currentResult!!.rating!!
//                    ).plus(progressView)
//                )
//            )
//                .enqueue(object : retrofit2.Callback<PlaceDetail> {
//                    override fun onResponse(
//                        call: Call<PlaceDetail>,
//                        response: Response<PlaceDetail>
//                    ) {
//                        mPlace = response!!.body()
//                        place_address.text = mPlace!!.results!!.formatted_address
//                        place_name.text = mPlace!!.results!!.name
//
//                    }
//
//                    override fun onFailure(call: Call<PlaceDetail>, t: Throwable?) {
//                        Toast.makeText(baseContext, "" + t!!.message, Toast.LENGTH_SHORT).show()
//
//                    }
//
//                })

//            mService.getDistanceMatrix(
//                getPlaceDistanceMatrixUrl(
//                    3.14306214387, 101.702601706,
//                    Common.currentResult!!.reference!!
//                )
//            ).enqueue(object : Callback<DistanceM> {
//                override fun onResponse(call: Call<DistanceM>, response: Response<DistanceM>) {
//                    mDistance = response!!.body()
//                    distance.text = mDistance!!.rows!![0].elements!![0].distance!!.text
//
//                }
//                override fun onFailure(call: Call<DistanceM>, t: Throwable?) {
//                    Toast.makeText(baseContext, "" + t!!.message, Toast.LENGTH_SHORT).show()
//                }
//            })
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun getPlaceDistanceMatrixUrl(
        originsLat: Double,
        originsLng: Double,
        destPlaceId: String
    ): String {
        val url = StringBuilder("https://maps.googleapis.com/maps/api/distancematrix/json")
        url.append("?origins=$originsLat,$originsLng")
        url.append("&destinations=place_id:$destPlaceId")
        url.append("&key=$API_KEY")
        return url.toString()
    }

//    private fun getPlaceDistance(originsLat: Double, originsLng: Double, destPlaceId: String): String {
//
//
//        mServices.getDistanceMatrix(
//            getPlaceDistanceMatrixUrl(
//                originsLat, originsLng,
//                destPlaceId
//            )
//        ).enqueue(object : Callback<DistanceM> {
//            override fun onResponse(call: Call<DistanceM>, response:Response<DistanceM>) {
//                mDistance = response!!.body()
//                disKM = mDistance!!.rows!![0].elements!![0].distance!!.text!!.toString()
//                //txtDistance.text = disKM
//                Log.d("RESPONSE", "$disKM")
//            }
//
//            override fun onFailure(call: Call<DistanceM>, t: Throwable?) {
//                Toast.makeText(baseContext, "" + t!!.message, Toast.LENGTH_SHORT).show()
//            }
//        })
//    }

    val url2 : StringBuilder ?=null
    private fun confirmation():String {

        val url = StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json")

        url.append("?location=$latitude1,$longtitude1")
        url.append("&radius=$result")
        url.append("&type=restaurant")
        url.append("&opennow=$openNow")
        url.append("&rating=$rating")
        url.append("&minprice=$minpriceLevel")
        url.append("&maxprice=$maxpriceLevel")
        url.append("&key=$API_KEY")

        println("button url: " + url.toString())



        return url.toString()


    }

    //user location
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        latitude1 = location.latitude
                        longtitude1 = location.longitude
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            findViewById<TextView>(R.id.latTextView).text = mLastLocation.latitude.toString()
            findViewById<TextView>(R.id.lonTextView).text = mLastLocation.longitude.toString()
        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }

    private fun nearByPlace() {

        //clear all marker in map
        // mMap.clear()
        var url:String = getUrl()
        println("Testing123 - " + url)

        mServices.getNearbyPlace(getUrl())
            .enqueue(object : Callback<MyPlaces> {
                override fun onResponse(call: Call<MyPlaces>?, response: Response<MyPlaces>?) {
                    currentPlace = response!!.body()!!

                    if (response!!.isSuccessful) {

                        for (i in 0 until response!!.body()!!.results!!.size) {

                            val markerOptions = MarkerOptions()
                            val googlePlace = response.body()!!.results!![i]
                            val lat = googlePlace.geometry!!.location!!.lat
                            val lng = googlePlace.geometry!!.location!!.lng
                            val placeName = googlePlace.name
                            val latLng = LatLng(lat, lng)

                            Common.currentResult = currentPlace!!.results!![i]


                            val placeId = Common.currentResult!!.reference!!
                            val name = Common.currentResult!!.name!!
                            val address = Common.currentResult!!.vicinity!!


                            val isOpenNow:Boolean
                            if(Common.currentResult!!.opening_hours != null){
                                isOpenNow = Common.currentResult!!.opening_hours!!.open_now
                            }else{
                                isOpenNow = false
                            }
                            //val isOpenNow = Common.currentResult!!.opening_hours!!.open_now!!
                            val priceLevel = Common.currentResult!!.price_level!!
                            val rating = Common.currentResult!!.rating!!

                            val noOfReview:Int
                            if(Common.currentResult!!.user_ratings_total != null){
                                noOfReview = Common.currentResult!!.user_ratings_total!!
                            }else{
                                noOfReview = 0
                            }

                            println("current result1: $placeId, $name, $address, $distance, $isOpenNow, $priceLevel, $rating, $noOfReview")

                            nearby.add(
                                Restaurant(
                                    placeId,
                                    name,
                                    address,
                                    isOpenNow,
                                    priceLevel,
                                    rating,
                                    noOfReview
                                )
                            )



                            println("array : $nearby")


                            // markerOptions.position(latLng)
                            // markerOptions.title(placeName)
                            // restaurantLocation = response.body()!!.results!![i]

                            /* if(typePlace.equals("market")){
                                 markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_market))}
                             else if(typePlace.equals("restaurant")){
                                 markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_restaurant))}
                             else{
                                 markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))}*/

                            markerOptions.snippet(i.toString())

                            // mMap!!.addMarker(markerOptions)
                            // mMap!!.moveCamera(CameraUpdateFactory.newLatLng(LatLng(lat, lng)))
                            // mMap!!.animateCamera(CameraUpdateFactory.zoomTo(11f))

                        }



                    }
                    Log.d("TESTING", " $nearby")
                }

                override fun onFailure(call: Call<MyPlaces>?, t: Throwable?) {

                    Toast.makeText(baseContext, "" + t!!.message, Toast.LENGTH_SHORT).show()
                }
            })



    }

    //seekBar
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        progressView!!.text = progress.toString()
        range(progress.toString())
    }

    private fun range(progress: String) {

        var progress2 = Integer.parseInt(progress)
         result = progress2 * 1000
    }


    override fun onStopTrackingTouch(seekBar: SeekBar) {

    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {

    }


    // MapsActivity
    private fun initMaps() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //val mapFragment = supportFragmentManager
        //    .findFragmentById(R.id.map) as SupportMapFragment
        //mapFragment.getMapAsync(this)

        mServices = Common.googleAPIService

        //have
        //request runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkLocationPermission()) {
                buildLocationRequest()
                buildLocationCallBack()
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.myLooper()
                )
            }
        } else {
            buildLocationRequest()
            buildLocationCallBack()
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            )
        }

        //nearByPlace("restaurant")
    }





    private fun getUrl(): String {

        val googlePlaceUrl =
            StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
        googlePlaceUrl.append("?location=$latitude1,$longtitude1")
        googlePlaceUrl.append("&radius=$result") //10km
        googlePlaceUrl.append("&type=restaurant")
        googlePlaceUrl.append("&opennow=$openNow")
        googlePlaceUrl.append("&rating=$rating")
        googlePlaceUrl.append("&minprice=$minpriceLevel")
        googlePlaceUrl.append("&maxprice=$maxpriceLevel")
        googlePlaceUrl.append("&key=${API_KEY}")


        Log.d("URL_DEBUG", googlePlaceUrl.toString())
        return googlePlaceUrl.toString()

    }


    //have
    private fun buildLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                mLastLocation = p0!!.locations.get(p0!!.locations.size - 1) //get last location

                if (mMarker != null) {
                    mMarker!!.remove()
                }

                latitude1 = mLastLocation.latitude
                longtitude1 = mLastLocation.longitude
                Log.d("TESTING", "buildLocationCallBack $latitude1, $longtitude1")
               // nearByPlace("restaurant")


                //val latLng = LatLng(latitude1, longtitude1)
                // val markerOptions = MarkerOptions()
                //    .position(latLng)
                //    .title("Your position")
                //    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                //  mMarker = mMap!!.addMarker(markerOptions)

                //  mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                // mMap!!.animateCamera(CameraUpdateFactory.zoomTo(11f))
            }
        }
    }

    //have
    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f
    }


    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), MY_PERMISSION_CODE
                )
            else
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), MY_PERMISSION_CODE
                )
            return false
        } else
            return true
    }


    //have
    @RequiresApi(Build.VERSION_CODES.M)
    fun onRequestPermissionResult(
        requestCode: Int,
        permission: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                        if (checkLocationPermission()) {
                            buildLocationRequest()
                            buildLocationCallBack()

                            fusedLocationProviderClient =
                                LocationServices.getFusedLocationProviderClient(this)
                            fusedLocationProviderClient.requestLocationUpdates(
                                locationRequest,
                                locationCallback,
                                Looper.myLooper()
                            );

                            mMap!!.isMyLocationEnabled = true
                        }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //have
    override fun onStop() {
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
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mMap!!.isMyLocationEnabled = true
            }
        } else
            mMap!!.isMyLocationEnabled = true


        mMap!!.setOnMarkerClickListener { marker ->


            if (marker.snippet != null) {

                Common.currentResult = currentPlace!!.results!![Integer.parseInt(marker.snippet)]
                println("current result456:" + Common.currentResult)

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




    fun Context.toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()


}


