package com.example.justeat


import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.justeat.Common.Common
import com.example.myapplication.Model.PlaceDetail
import com.example.myapplication.Remote.iGoogleAPIService
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.restaurant.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class ViewRestaurant : AppCompatActivity() {

    internal lateinit var mService:iGoogleAPIService
    var mPlace:PlaceDetail?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.restaurant)

        mService = Common.googleAPIService


        //set empty
        place_name.text=""
        place_address.text=""
        place_open_hour.text=""

        btn_show_map.setOnClickListener{
            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mPlace!!.results!!.url))
            startActivity(mapIntent)
        }

        if(Common.currentResult!!.photos != null && Common.currentResult!!.photos!!.size>0){
            Picasso.get()
                .load(getPhotoOfPlace(Common.currentResult!!.photos!![0].photo_reference!!, 1000))
                .into(photo)
        }
        if(Common.currentResult!!.rating != null)
            ratingBar.rating = Common.currentResult!!.rating.toFloat()
        else
            ratingBar.visibility = View.GONE

        if(Common.currentResult!!.opening_hours != null)
            place_open_hour.text="Open now: " +Common.currentResult!!.opening_hours!!.open_now
        else
            place_open_hour.visibility = View.GONE

        mService.getDetailPlace(getPlaceDetailUrl(Common.currentResult!!.places_id!!))
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

    }

    private fun getPlaceDetailUrl(placesId: String): String {
        val url = StringBuilder("https://maps.googleapis.com/maps/api/place/details/json")
        url.append("?place_id=$placesId")
        url.append("&key=AIzaSyCpSy9Q9WDmK1YFR9eUz3r9uP7g9nOGD-s")
        return url.toString()
    }

    private fun getPhotoOfPlace(photoReference: String, maxWidth: Int): String {
        val url = StringBuilder("https://maps.googleapis.com/maps/api/place/photo")
        url.append("?maxWidth=$maxWidth")
        url.append("&photoreference=$photoReference")
        url.append("&key=AIzaSyCpSy9Q9WDmK1YFR9eUz3r9uP7g9nOGD-s")
        return url.toString()
    }
}