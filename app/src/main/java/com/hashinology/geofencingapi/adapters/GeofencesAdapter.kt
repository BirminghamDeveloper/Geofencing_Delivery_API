package com.hashinology.geofencingapi.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.hashinology.geofencingapi.data.GeofenceEntity
import com.hashinology.geofencingapi.databinding.GeofencesRowLayoutBinding
import com.hashinology.geofencingapi.util.MyDiffutil
import com.hashinology.geofencingapi.viewmodels.SharedViewModel
import kotlinx.coroutines.launch

class GeofencesAdapter(private val sharedVM: SharedViewModel): RecyclerView.Adapter<GeofencesAdapter.MyViewHolder>() {
    private var geofenceEntityList = mutableListOf<GeofenceEntity>()
    /*// Old Style:
    private var geofenceEntity: MutableList<GeofenceEntity> = mutableListOf()*/

    class MyViewHolder(val binding: GeofencesRowLayoutBinding): RecyclerView.ViewHolder(binding.root) {
        companion object{
            fun from(parent: ViewGroup): MyViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = GeofencesRowLayoutBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
        fun bind(geofenceEntity: GeofenceEntity){
            binding.geofencesEntity = geofenceEntity
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GeofencesAdapter.MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: GeofencesAdapter.MyViewHolder, position: Int) {
        val currentGeofence = geofenceEntityList[position]
        holder.bind(currentGeofence)
        holder.binding.deleteImageView.setOnClickListener {
//            Toast.makeText(holder.itemView.context, "Clicked", Toast.LENGTH_LONG).show()
            removeItem(holder, position)
        }

    }

    private fun removeItem(holder: GeofencesAdapter.MyViewHolder, position: Int) {
        sharedVM.viewModelScope.launch {
            val geofenceStopped =
                sharedVM.stopGeofence(listOf(geofenceEntityList[position].geoId))
            if (geofenceStopped){
                sharedVM.removeGeofence(geofenceEntity = geofenceEntityList[position])
                sharedVM.geofenceRemoved = true
                showSnackBar(holder, geofenceEntityList[position])
            }else{
                Log.d("GeofenceAdapter", "Geofence NOT REMOVED! ")
            }
        }
    }

    private fun showSnackBar(
        holder: MyViewHolder,
        removedItem: GeofenceEntity
    ) {
        Snackbar.make(
            holder.itemView,
            "Removed: "+ removedItem.name,
            Snackbar.LENGTH_LONG
        ).setAction("UNDO"){
            undoRemoval(holder.itemView, removedItem)
        }.show()
    }

    private fun undoRemoval(itemView: View, removedItem: GeofenceEntity) {
        sharedVM.addGeofence(removedItem)
        sharedVM.startGeofence(
            removedItem.latitude,
            removedItem.longitude
        )
        sharedVM.geofenceRemoved = false
    }

    override fun getItemCount(): Int {
        return geofenceEntityList.size
    }

    fun setData(newGeofenceEntity: MutableList<GeofenceEntity>){
        val geofenceDiffUtil = MyDiffutil(geofenceEntityList, newGeofenceEntity)
        val diffUtilResult = DiffUtil.calculateDiff(geofenceDiffUtil)
        geofenceEntityList = newGeofenceEntity
        diffUtilResult.dispatchUpdatesTo(this)
    }

}