package com.example.justeat

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {

     var progressView: TextView? = null
    var seekBarView : SeekBar?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Seekbar
        progressView = this.textView2
        seekBarView = this.seekBar
        seekBarView!!.setOnSeekBarChangeListener(this)

        // Set chip group checked change listener
        chipGroup2.setOnCheckedChangeListener{group,checkedId:Int ->
            // Get the checked chip instance from chip group
            val chip:Chip? = findViewById(checkedId)

            chip?.let {
                // Show the checked chip text on toast message
                toast("${it.text} checked")
            }
        }

        chipGroup4.setOnCheckedChangeListener{group,checkId:Int ->

            val chip:Chip? = findViewById(checkId)
            chip?.let{

                chip?.setChipBackgroundColorResource(R.color.lightBlue)
            }
        }

    }

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

