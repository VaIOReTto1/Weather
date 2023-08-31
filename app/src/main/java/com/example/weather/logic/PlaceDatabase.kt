package com.example.weather.logic

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.weather.logic.model.Location
import com.example.weather.logic.model.Place

class PlaceDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    init{

    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "place_history.db"
        private const val TABLE_NAME = "place_history"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_LAT = "lat"
        private const val COLUMN_LNG = "lng"
        private const val COLUMN_ADDRESS = "address"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_NAME TEXT, $COLUMN_LAT TEXT, $COLUMN_LNG TEXT,$COLUMN_ADDRESS TEXT)"
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    fun insertPlace(place: Place) {
        val existingPlace = getPlaceByName(place.name)
        if (existingPlace != null) {
            deletePlace(existingPlace)
        }

        val values = ContentValues().apply {
            put(COLUMN_NAME, place.name)
            put(COLUMN_LAT, place.location.lat)
            put(COLUMN_LNG, place.location.lng)
            put(COLUMN_ADDRESS, place.address)
        }
        writableDatabase.insert(TABLE_NAME, null, values)
        Log.d("PlaceDatabase", "Place inserted: ${place.name}")
    }

    //判断是否已存在该数据
    private fun getPlaceByName(name: String): Place? {
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_NAME = ?"
        val cursor = readableDatabase.rawQuery(query, arrayOf(name))
        val place = if (cursor.moveToFirst()) {
            val lat = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAT))
            val lng = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LNG))
            val address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS))
            Place(name, Location(lat, lng), address)
        } else {
            null
        }
        cursor.close()
        return place
    }

    fun deletePlace(place: Place) {
        val whereClause = "$COLUMN_NAME = ?"
        val whereArgs = arrayOf(place.name)
        writableDatabase.delete(TABLE_NAME, whereClause, whereArgs)
    }

    fun getHistoryPlaces(): List<Place> {
        val placeList = mutableListOf<Place>()
        val cursor = readableDatabase.query(
            TABLE_NAME, null, null, null, null, null, "$COLUMN_ID DESC"
        )
        with(cursor) {
            while (moveToNext()) {
                val name = getString(getColumnIndexOrThrow(COLUMN_NAME))
                val lat = getString(getColumnIndexOrThrow(COLUMN_LAT))
                val lng = getString(getColumnIndexOrThrow(COLUMN_LNG))
                val address=getString(getColumnIndexOrThrow(COLUMN_ADDRESS))
                placeList.add(Place(name, Location(lng, lat), address))
            }
            close()
        }
        return placeList
    }

    fun clearHistory() {
        writableDatabase.delete(TABLE_NAME, null, null)
    }


}
