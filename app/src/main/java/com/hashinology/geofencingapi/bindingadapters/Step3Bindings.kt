package com.hashinology.geofencingapi.bindingadapters

import android.annotation.SuppressLint
import android.widget.SeekBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.hashinology.geofencingapi.viewmodels.SharedViewModel
import com.hashinology.geofencingapi.R


@BindingAdapter("bindSeekBar")
fun bindSeekBar(seekBar: SeekBar, sharedVM: SharedViewModel) {
    // Initialize SeekBar progress from ViewModel
    val initialProgress = (sharedVM.GeoRadius.value?.toInt() ?: 500) / 500
    seekBar.progress = initialProgress

    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                var value = 500 + (progress * 500)  // Convert progress to actual radius [[3]][[7]]
                sharedVM.GeoRadius.value = value.toFloat()
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        override fun onStopTrackingTouch(seekBar: SeekBar) {}
    })
}

@SuppressLint("StringFormatMatches")
@BindingAdapter("formattedGeoRadius")
fun TextView.setFormattedGeoRadius(geoRadius: Float) {
    val value = geoRadius.toDouble()
    val kilometers = value / 1000
    text = if (value >= 1000) {
        context.getString(R.string.display_kilometers, kilometers)
    } else {
        context.getString(R.string.display_meters, value.toInt())
    }
}