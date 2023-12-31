package com.example.weather.ui.place

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.MainActivity
import com.example.weather.R
import com.example.weather.logic.PlaceDatabase
import com.example.weather.logic.model.Place
import com.example.weather.ui.weather.WeatherActivity

class PlaceFragment : Fragment() {

    val viewModel by lazy { ViewModelProvider(this)[PlaceViewModel::class.java] }

    private lateinit var adapter: PlaceAdapter

    private lateinit var historyAdapter: PlaceHistoryAdapter

    lateinit var placeDatabaseHelper: PlaceDatabase

    private var lastSearchText = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_place, container, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(activity)

        //判断是否为初次打开软件
        if ((activity as MainActivity).open=="1") {
            val intent = Intent(context, WeatherActivity::class.java)
            startActivity(intent)
            activity?.finish()
            return
        }

        val recyclerView: RecyclerView = view.findViewById(R.id.recycleView)
        val searchPlaceEdit: EditText = view.findViewById(R.id.searchPlaceEdit)
        val bgImageView: ImageView = view.findViewById(R.id.bgImageView)

        recyclerView.visibility = View.GONE
        bgImageView.visibility = View.VISIBLE

        placeDatabaseHelper = PlaceDatabase(requireContext())

        recyclerView.layoutManager = layoutManager
        val historyPlaces = placeDatabaseHelper.getHistoryPlaces()
        adapter = PlaceAdapter(this, viewModel.placeList)
        historyAdapter = PlaceHistoryAdapter(this, historyPlaces)
        if (historyPlaces.isNotEmpty()) {
            recyclerView.visibility = View.VISIBLE
            bgImageView.visibility = View.GONE
            recyclerView.adapter = historyAdapter
        }

        //判断显示历史记录还是搜索结果
        searchPlaceEdit.setOnFocusChangeListener { _, hasFocus ->
            viewModel.placeList.clear()
            if (hasFocus) {
                recyclerView.visibility = View.GONE
                bgImageView.visibility = View.VISIBLE
            } else {
                // Load and display history records
                bgImageView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                if (historyPlaces.isNotEmpty()) {
                    recyclerView.visibility = View.VISIBLE
                    bgImageView.visibility = View.GONE
                    recyclerView.adapter = historyAdapter
                }
            }

            searchPlaceEdit.addTextChangedListener { editable ->
                val context = editable.toString()
                recyclerView.visibility = View.VISIBLE
                bgImageView.visibility = View.GONE
                if (context!=lastSearchText) {
                    viewModel.searchPlacces(context)
                    adapter.notifyDataSetChanged()
                    recyclerView.adapter = adapter
                    recyclerView.visibility = View.VISIBLE
                    bgImageView.visibility = View.GONE
                }
                else {
                    adapter.notifyDataSetChanged()
                    recyclerView.adapter = historyAdapter
                }
            }

            viewModel.placeLiveData.observe(viewLifecycleOwner) { result ->
                val places = result.getOrNull()
                if (places != null) {
                    viewModel.placeList.clear()
                    viewModel.placeList.addAll(places)

                    if (searchPlaceEdit.text.isNotEmpty()) {
                        recyclerView.adapter = adapter
                        adapter.notifyDataSetChanged()
                        recyclerView.visibility = View.VISIBLE
                        bgImageView.visibility = View.GONE
                    }
                } else {
                    recyclerView.adapter = historyAdapter
                    recyclerView.visibility = View.VISIBLE
                    bgImageView.visibility = View.GONE
                    Toast.makeText(activity, "未能查询到任何地点", Toast.LENGTH_SHORT).show()
                    result.exceptionOrNull()?.printStackTrace()
                }
            }

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun deletePlace(place: Place) {
        val database = PlaceDatabase(requireContext())
        database.deletePlace(place)
        val historyPlaces = placeDatabaseHelper.getHistoryPlaces()
        historyAdapter.placeList = historyPlaces
        historyAdapter.notifyDataSetChanged()
    }
}