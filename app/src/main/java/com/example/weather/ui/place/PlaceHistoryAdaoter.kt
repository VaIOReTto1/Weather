package com.example.weather.ui.place

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.logic.model.Place
import com.example.weather.ui.weather.WeatherActivity

class PlaceHistoryAdapter(private val fragment: PlaceFragment, private val placeList: List<Place>) :
    RecyclerView.Adapter<PlaceHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View):RecyclerView.ViewHolder(view){
        val placeName:TextView=view.findViewById(R.id.placeHistoryName)
        val placeAddress:TextView=view.findViewById(R.id.placeHistoryAddress)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.place_history_item, parent, false)

        val holder=ViewHolder(view)
        holder.deleteButton.setOnClickListener {
            val activity=fragment.activity
            if (activity is WeatherActivity) {
                val position = holder.adapterPosition
                val place = placeList[position]
                // Call a method to delete the place from the database
                if (place.name==activity.viewModel.placeName)
                    Toast.makeText(activity,"不能删去正在显示的天气",Toast.LENGTH_SHORT).show()
//                else{
//                    activity.findViewById<DrawerLayout>(R.id.drawerLayout).closeDrawers()
//                    fragment.deletePlace(place)
//                    notifyItemRemoved(position)
//                }
            }
        }
        holder.itemView.setOnClickListener {
            val position=holder.adapterPosition
            val place=placeList[position]
            val activity=fragment.activity
            if (activity is WeatherActivity){
                activity.viewModel.locationLng=place.location.lng
                activity.viewModel.locationLat=place.location.lat
                activity.viewModel.placeName=place.name
//                activity.findViewById<DrawerLayout>(R.id.drawerLayout).closeDrawers()
            }else{
                val intent= Intent(parent.context,WeatherActivity::class.java).apply {
                    putExtra("location_lng",place.location.lng)
                    putExtra("location_lat",place.location.lat)
                    putExtra("place_name",place.name)
                }
                fragment.startActivity(intent)
                activity?.finish()
            }
            fragment.viewModel.savePlace(place)
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place=placeList[position]
        holder.placeName.text=place.name
        holder.placeAddress.text=place.address
    }

    override fun getItemCount()=placeList.size
}