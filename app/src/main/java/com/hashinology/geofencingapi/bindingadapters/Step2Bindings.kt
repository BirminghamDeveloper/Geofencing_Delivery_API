package com.hashinology.geofencingapi.bindingadapters

import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.hashinology.geofencingapi.util.ExtensionFunctions.hide
import com.hashinology.geofencingapi.util.ExtensionFunctions.show

@BindingAdapter("handleNetworkConnection", "handleRecyclerView", requireAll = true)
fun TextInputLayout.handleNetworkConnection(networkAvailable: Boolean, recyclerView: RecyclerView) {
    if (!networkAvailable) {
        this.isErrorEnabled = true
        this.error = "No Internet Connection."
        recyclerView.hide()
    } else {
        this.isErrorEnabled = false
        this.error = null
        recyclerView.show()
    }
}
/*fun TextInputLayout.handleNetworkConnection(
    networkAvailable: LiveData<Boolean>, // Now accepts LiveData<Boolean>
    recyclerView: RecyclerView
) {
    // Observe the LiveData to react to changes
    networkAvailable.observeForever { isConnected ->
        if (!isConnected) {
            this.isErrorEnabled = true
            error = "No Internet Connection."
            recyclerView.hide()
        } else {
            isErrorEnabled = false
            error = null
            recyclerView.show()
        }
        }
    }*/
