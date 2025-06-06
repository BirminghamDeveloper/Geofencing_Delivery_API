package com.hashinology.geofencingapi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.hashinology.geofencingapi.databinding.PredictionsRowLayoutBinding
import com.hashinology.geofencingapi.util.MyDiffutil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PredictionsAdapter: RecyclerView.Adapter<PredictionsAdapter.MyViewHolder>() {

    private var predictionList = emptyList<AutocompletePrediction>()

    private val _placeId = MutableStateFlow("")
    val placeId: StateFlow<String> get() = _placeId

    class MyViewHolder(val binding: PredictionsRowLayoutBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(prediction: AutocompletePrediction){
            binding.prediction = prediction
        }
        companion object{
            fun from(parent: ViewGroup): MyViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = PredictionsRowLayoutBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun getItemCount(): Int {
        return predictionList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentPrediction = predictionList[position]
        holder.bind(currentPrediction)

        holder.binding.rootLayout.setOnClickListener {
            setPlaceID(predictionList[position].placeId)
        }
    }

    private fun setPlaceID(placeID: String){
        _placeId.value = placeID
    }

    fun setData(newPredictionList: List<AutocompletePrediction>){
        /*// this is the old style code:
        notifyDataSetChanged()*/

        val myDiffutil = MyDiffutil(predictionList, newPredictionList)
        val myDiffutilResult = DiffUtil.calculateDiff(myDiffutil)
        predictionList = newPredictionList

        myDiffutilResult.dispatchUpdatesTo(this)
    }
}