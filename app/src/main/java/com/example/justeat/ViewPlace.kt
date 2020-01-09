package com.example.justeat

import DistanceM
import android.content.Intent
import android.graphics.Color
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

class ViewPlace : AppCompatActivity() {

    internal  lateinit var mService:iGoogleAPIService
    var mPlace:PlaceDetail?=null
    var mDistance : DistanceM ?=null
    companion object{
        private const val API_KEY = "AIzaSyABCcNkZ4q2DH34jM_IIzsQ4m9-ury_Ph0"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Init service
        mService = Common.googleAPIService


        //set empty for all text view
        place_name.text=""
        place_address.text=""
        place_open_hour.text=""

        btn_show_map.setOnClickListener{
            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mPlace!!.results!!.url))
            startActivity(mapIntent)
            Toast.makeText(baseContext,"this is not call",Toast.LENGTH_SHORT).show()
        }

        call_now.setOnClickListener{
            val number = "tel:" + mPlace!!.results!!.formatted_phone_number
            //Toast.makeText(baseContext,"" + mPlace!!.results!!.formatted_phone_number,Toast.LENGTH_SHORT).show()
            val callIntent = Intent(Intent.ACTION_DIAL)
            callIntent.setData(Uri.parse("tel:" + mPlace!!.results!!.formatted_phone_number))
            startActivity(callIntent)

        }

        more_details.setOnClickListener{
            startActivity(Intent(baseContext, MainActivity::class.java))
        }


        if(Common.currentResult!!.name != null){
            place_name.text = Common.currentResult!!.name
        }

        if(Common.currentResult!!.vicinity != null){
            place_address.text = Common.currentResult!!.vicinity
            Toast.makeText(baseContext,"have address",Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(baseContext,"No address",Toast.LENGTH_SHORT).show()
        }

        if(Common.currentResult!!.user_ratings_total != null){
            review.text = "(" + Common.currentResult!!.user_ratings_total + "reviews)"
        }else{
            Toast.makeText(baseContext,"cannot get reviews",Toast.LENGTH_SHORT).show()
        }

        if(Common.currentResult!!.rating != null)
            ratingBar.rating = Common.currentResult!!.rating.toFloat()
        else
            ratingBar.visibility = View.GONE

        if(Common.currentResult!!.opening_hours != null){
            if(Common.currentResult!!.opening_hours!!.open_now.equals(false)){
                place_open_hour.setTextColor(Color.RED)
                place_open_hour.text="Closed now"
            }else{
                place_open_hour.setTextColor(Color.GREEN)
                place_open_hour.text="Open Now"
            }
            //place_open_hour.text="Open now: " +Common.currentResult!!.opening_hours!!.open_now
        }
        else {
            place_open_hour.visibility = View.GONE
        }

        mService.getDetailPlace(getPlaceDetailUrl(Common.currentResult!!.reference!!))
            .enqueue(object : Callback<PlaceDetail>{
                override fun onResponse(call: Call<PlaceDetail>, response: Response<PlaceDetail>) {
                    mPlace = response.body()
                    //Toast.makeText(baseContext,""+Common.currentResult!!.formatted_phone_number,Toast.LENGTH_SHORT).show()
                    try{
                        if(mPlace!!.results!!.formatted_phone_number != null){
                            Toast.makeText(baseContext,"maplce got number",Toast.LENGTH_SHORT).show()
                        }
                    }catch (e:Exception){
                        Toast.makeText(baseContext,"maplce no number",Toast.LENGTH_SHORT).show()
                    }
                    try{
                        if(mPlace!!.results!!.photos!![0].photo_reference != null){
                            Toast.makeText(baseContext,"maplce got photo",Toast.LENGTH_SHORT).show()
                        }
                    }catch (e:Exception){
                        Toast.makeText(baseContext,"maplce no number",Toast.LENGTH_SHORT).show()
                    }
                    if(mPlace!!.results!!.photos != null && mPlace!!.results!!.photos!!.isNotEmpty()){
                        Picasso.get()
                            .load(getPhotoOfPlace( mPlace!!.results!!.photos!![0].photo_reference!!, 800))
                            .resize(800,600)
                            .into(photo)
                        Toast.makeText(baseContext,"photo not working",Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(baseContext,"No photo",Toast.LENGTH_SHORT).show()
                    }


                    //Toast.makeText(baseContext,"vincinity is empty",Toast.LENGTH_SHORT).show()
                    //place_address.text = mPlace!!.results!!.formatted_address
                    //place_name.text = mPlace!!.results!!.name

                }

                override fun onFailure(call: Call<PlaceDetail>, t: Throwable?) {
                    Toast.makeText(baseContext,""+t!!.message,Toast.LENGTH_SHORT).show()

                }

            })

        mService.getDistanceMatrix(getPlaceDistanceMatrixUrl(R.id.latTextView.toDouble(),
            R.id.lonTextView.toDouble(), Common.currentResult!!.reference!!)).enqueue(object : Callback<DistanceM> {
            override fun onResponse(call: Call<DistanceM>, response: Response<DistanceM>) {
                mDistance = response!!.body()
                distance.text = mDistance!!.rows!![0].elements!![0].distance!!.text

            }
            override fun onFailure(call: Call<DistanceM>, t: Throwable?) {
                Toast.makeText(baseContext, "" + t!!.message, Toast.LENGTH_SHORT).show()
            }
        })



        /*mService.getDetailPlace(getPlaceDetailUrl(Common.currentResult!!.places_id!!))
            .enqueue(object : Callback<PlaceDetail>{
                override fun onResponse(call: Call<PlaceDetail>, response: Response<PlaceDetail>) {
                    mPlace = response!!.body()
                    place_address.text = mPlace!!.results!!.formatted_address
                    place_name.text = mPlace!!.results!!.name
                }
                override fun onFailure(call: Call<PlaceDetail>, t: Throwable?) {
                    Toast.makeText(baseContext,""+t!!.message,Toast.LENGTH_SHORT).show()
                }
            })*/

    }



    private fun getPlaceDetailUrl(reference: String): String {
        val url = StringBuilder("https://maps.googleapis.com/maps/api/place/details/json")
        url.append("?place_id=$reference")
        url.append("&key=$API_KEY")
        return url.toString()
    }

    private fun getPhotoOfPlace(photoReference: String, maxWidth: Int): String {
        val url = StringBuilder("https://maps.googleapis.com/maps/api/place/photo")
        url.append("?maxwidth=$maxWidth")
        url.append("&photoreference=$photoReference")
        url.append("&key=$API_KEY")
        return url.toString()
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
}