package com.hashinology.geofencingapi.bindingadapters

import android.annotation.SuppressLint
import android.widget.SeekBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.slider.Slider
import com.hashinology.geofencingapi.viewmodels.SharedViewModel
import com.hashinology.geofencingapi.R


@BindingAdapter("updateSliderValueTextView", "getGeoRadius", requireAll = true)
fun Slider.updateSliderValue(textView: TextView, sharedViewModel: SharedViewModel) {
    updateSliderValueTextView(sharedViewModel.geoRadius , textView)
    this.addOnChangeListener { _, value, _ ->
        sharedViewModel.geoRadius = value
        updateSliderValueTextView(sharedViewModel.geoRadius, textView)
    }
}

fun Slider.updateSliderValueTextView(geoRadius: Float, textView: TextView) {
    val kilometers = geoRadius / 1000
    if (geoRadius >= 1000f) {
        textView.text = context.getString(R.string.display_kilometers, kilometers.toString())
    } else {
        textView.text = context.getString(R.string.display_meters, geoRadius.toString())
    }
    this.value = geoRadius
}