package com.example.justeat

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.justeat.Common.Common
import com.example.myapplication.Model.OpeningHours
import com.google.android.gms.location.*
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.activity_main.*
import com.example.myapplication.Model.PlaceDetail
import com.example.myapplication.Remote.iGoogleAPIService
import kotlinx.android.synthetic.main.restaurant.*
import retrofit2.Call
import retrofit2.Response
import java.lang.StringBuilder
import kotlin.math.absoluteValue


private const val PERMISSION_REQUEST = 10

class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {



    //seekbar
    var progressView: TextView? = null
    var seekBarView: SeekBar? = null

    //recycleView for checkbox



    //user location
    val PERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    //map
    internal lateinit var mService: iGoogleAPIService
    var mPlace: PlaceDetail?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        MultiDex.install(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mService = Common.googleAPIService
       // place_open_hour.text=""
       // val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mPlace!!.results!!.url))
       // startActivity(mapIntent)

        //user location
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getLastLocation()

        //switch button
        val sw = findViewById<Switch>(R.id.switch1)
        sw?.setOnCheckedChangeListener { _, isChecked ->
            val msg = if (isChecked) "ON" else "OFF"
            if(isChecked){
                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                if(Common.currentResult!!.opening_hours != null){
                    openNow(Common.currentResult!!.opening_hours!!)

                }else{

                }


            }else{
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

            if(checkedId==R.id.chip){
                chip?.setChipBackgroundColorResource(R.color.lightBlue)
                if(Common.currentResult!!.rating != null )
                    ratingBar.rating = Common.currentResult!!.rating.toFloat()
                rating(ratingBar.rating)
            }else if(checkedId == R.id.chip2){
                chip?.setChipBackgroundColorResource(R.color.lightBlue)
                if(Common.currentResult!!.rating != null && Common.currentResult!!.rating >= 5.0)
                    ratingBar.rating = Common.currentResult!!.rating.toFloat()
                rating(ratingBar.rating)
            }else if(checkedId == R.id.chip3){
                chip?.setChipBackgroundColorResource(R.color.lightBlue)
                if(Common.currentResult!!.rating != null)
                    if(Common.currentResult!!.rating >= 4.0 && Common.currentResult!!.rating<5.0)
                    ratingBar.rating = Common.currentResult!!.rating.toFloat()
                rating(ratingBar.rating)
            }else if(checkedId == R.id.chip4){
                chip?.setChipBackgroundColorResource(R.color.lightBlue)
                if(Common.currentResult!!.rating != null)
                    if(Common.currentResult!!.rating >= 3.0&& Common.currentResult!!.rating<4.0)
                        ratingBar.rating = Common.currentResult!!.rating.toFloat()
                rating(ratingBar.rating)
            }else if(checkedId == R.id.chip5){
                chip?.setChipBackgroundColorResource(R.color.lightBlue)
                if(Common.currentResult!!.rating != null)
                    if(Common.currentResult!!.rating >= 2.0&& Common.currentResult!!.rating<3.0)
                        ratingBar.rating = Common.currentResult!!.rating.toFloat()
                rating(ratingBar.rating)
            }else if(checkedId == R.id.chip6){
                chip?.setChipBackgroundColorResource(R.color.lightBlue)
                if(Common.currentResult!!.rating != null)
                    if(Common.currentResult!!.rating >= 1.0&& Common.currentResult!!.rating<2.0)
                        ratingBar.rating = Common.currentResult!!.rating.toFloat()
                rating(ratingBar.rating)
            }else{
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
            if(checkId == R.id.chip7) {
                //chip?.setChipBackgroundColorResource(R.color.lightBlue)

                    Common.currentResult!!.price_level < 100
                        price.text = "Price: " + Common.currentResult!!.price_level
                priceRange(Common.currentResult!!.price_level)

            }else if(checkId == R.id.chip8){
                chip?.setChipBackgroundColorResource(R.color.lightBlue)
                if (Common.currentResult!!.price_level != null)
                    if (Common.currentResult!!.price_level < 1000 && Common.currentResult!!.price_level>=100)
                        price.text = "Price: " + Common.currentResult!!.price_level
                priceRange(Common.currentResult!!.price_level)
            }else if(checkId == R.id.chip9){
                chip?.setChipBackgroundColorResource(R.color.lightBlue)
                if (Common.currentResult!!.price_level != null)
                    if (Common.currentResult!!.price_level < 10000 && Common.currentResult!!.price_level>=1000)
                        price.text = "Price: " + Common.currentResult!!.price_level
                priceRange(Common.currentResult!!.price_level)
            }

            //chip?.let {
              //  chip?.setChipBackgroundColorResource(R.color.lightBlue)
           // }
        }

        //get filter restaurant





    }

    private fun openNow(openingHours: OpeningHours):String {

            val url= StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/opennow&key=AIzaSyABCcNkZ4q2DH34jM_IIzsQ4m9-ury_Ph0")
            url.append("?opening_hours=$openingHours")
            return url.toString()

    }


    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.confirm -> {
            // User chose the "Settings" item, show the app settings UI...

            mService.getDetailPlace(confirmation(Common.currentResult!!.opening_hours!!.open_now.toString().plus(Common.currentResult!!.rating!!).plus(progressView)))
                .enqueue(object : retrofit2.Callback<PlaceDetail>{
                    override fun onResponse(call: Call<PlaceDetail>, response: Response<PlaceDetail>) {
                        mPlace = response!!.body()
                        place_address.text = mPlace!!.results!!.formatted_address
                        place_name.text = mPlace!!.results!!.name

                    }

                    override fun onFailure(call: Call<PlaceDetail>, t: Throwable?) {
                        Toast.makeText(baseContext,""+t!!.message,Toast.LENGTH_SHORT).show()

                    }

                })
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }



    private fun confirmation(plus: String): String {
        val url= StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/opennow&result&rating&minprice&maxprice&key=AIzaSyABCcNkZ4q2DH34jM_IIzsQ4m9-ury_Ph0")
        url.append("?restaurant=$plus")
        return url.toString()
    }

    private fun priceRange(priceLevel: Int):String {
        if(priceLevel<=100){
        val url= StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/minprice=0&maxprice=1&key=AIzaSyABCcNkZ4q2DH34jM_IIzsQ4m9-ury_Ph0")
        url.append("?price=$priceLevel")
        return url.toString()
        }else if(priceLevel<=1000){
            val url= StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/minprice=1&maxprice=2&key=AIzaSyABCcNkZ4q2DH34jM_IIzsQ4m9-ury_Ph0")
            url.append("?price=$priceLevel")
            return url.toString()
        }else if(priceLevel<=10000) {
            val url =
                StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/minprice=2&maxprice=3&key=AIzaSyABCcNkZ4q2DH34jM_IIzsQ4m9-ury_Ph0")
            url.append("?price=$priceLevel")
            return url.toString()
        }else{
            val url= StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/minprice=0&key=AIzaSyABCcNkZ4q2DH34jM_IIzsQ4m9-ury_Ph0")
            url.append("?price=$priceLevel")
            return url.toString()
        }
    }

    private fun rating(rating: Float):String {
        val url= StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/rating&key=AIzaSyABCcNkZ4q2DH34jM_IIzsQ4m9-ury_Ph0")
        url.append("?rating=$rating")
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
                        findViewById<TextView>(R.id.latTextView).text = location.latitude.toString()
                        findViewById<TextView>(R.id.lonTextView).text = location.longitude.toString()
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
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }

    //checkBox Adapter

    //seekBar
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        progressView!!.text = progress.toString()
        range(progress.toString())
    }

    private fun range(progress: String) :String{

        var progress2 = Integer.parseInt(progress)
        var result =  progress2 *1000
        val url= StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/result&key=AIzaSyABCcNkZ4q2DH34jM_IIzsQ4m9-ury_Ph0")
        url.append("?range=$result")
        return url.toString()

    }


    override fun onStopTrackingTouch(seekBar: SeekBar) {

    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {

    }

    fun Context.toast(message:String)=
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()




}


