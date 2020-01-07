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
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.checkbox.*

private const val PERMISSION_REQUEST = 10

class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {
    //seekbar
    var progressView: TextView? = null
    var seekBarView: SeekBar? = null

    //recycleView for checkbox
    private val list: RecyclerView? = null
    private var recyclerAdapter: adapter? = null


    //user location
    val PERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //user location
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getLastLocation()

        //switch button
        val sw1 = findViewById<Switch>(R.id.switch1)

        sw1?.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Switch:ON" else "Switch:OFF"
            Toast.makeText(this@MainActivity, message,
                Toast.LENGTH_SHORT).show()

        }

        // Seekbar
        progressView = this.textView2
        seekBarView = this.seekBar
        seekBarView!!.setOnSeekBarChangeListener(this)

        // Set chip group checked change listener
        chipGroup2.setOnCheckedChangeListener { group, checkedId: Int ->
            // Get the checked chip instance from chip group
            val chip: Chip? = findViewById(checkedId)

            chip?.let {
                chip?.setChipBackgroundColorResource(R.color.lightBlue)
                // Show the checked chip text on toast message
                toast("${it.text} checked")
            }
        }
        //chip group

        chipGroup4.setOnCheckedChangeListener { group, checkId: Int ->

            val chip: Chip? = findViewById(checkId)
            chip?.let {
                chip?.setChipBackgroundColorResource(R.color.lightBlue)
            }
        }

        //checkBox
        val list = findViewById<RecyclerView>(R.id.listCat)
        val category = arrayListOf<String>()
        category.add("All")
        category.add("Malaysian Food")
        category.add("Fast Food")
        category.add("Indian")
        category.add("Halal")
        category.add("Western")
        category.add("Chinese")

        val layoutManager = LinearLayoutManager(this)
        list.layoutManager = layoutManager
        recyclerAdapter = adapter(this@MainActivity, category)
        list.addItemDecoration(CustomDividerItemDecoration(this@MainActivity))
        list.adapter = recyclerAdapter




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
    private inner class CustomDividerItemDecoration(context: Context) :
        RecyclerView.ItemDecoration() {
        private val drawableline: Drawable?

        init {
            drawableline =
                ContextCompat.getDrawable(context,R.drawable.checkbox)
        }

        override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            val left = parent.paddingLeft
            val right = parent.width - parent.paddingRight
            val childCount = parent.childCount
            for (i in 0 until childCount) {
                val child = parent.getChildAt(i)
                val params = child.layoutParams as RecyclerView.LayoutParams
                val top = child.bottom + params.bottomMargin
                val bottom = top + drawableline!!.intrinsicHeight
                drawableline.setBounds(left, top, right, bottom)
                drawableline.draw(c)
            }
        }
    }

    private inner class adapter(internal var context: Context, internal var mData: List<String>) :
        RecyclerView.Adapter<adapter.myViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): adapter.myViewHolder {
            val view =
                LayoutInflater.from(context).inflate(R.layout.checkbox, parent, false)
            return myViewHolder(view)
        }

        override fun onBindViewHolder(holder: adapter.myViewHolder, position: Int) {
            holder.category.text = mData[position]
        }

        override fun getItemCount(): Int {
            return mData.size
        }

        inner class myViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            internal var category: TextView

            init {
                category = itemView.findViewById(R.id.cat)
            }
        }
    }

    //seekBar
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        progressView!!.text = progress.toString()
    }
    override fun onStopTrackingTouch(seekBar: SeekBar) {

    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {

    }

    fun Context.toast(message:String)=
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()





}

