package com.threebars.worldclock.dao;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.threebars.worldclock.CityTimeZone;

import static com.threebars.worldclock.dao.MyDatabaseHelper.COLUMN_NAMES.*;

public class CitiesDAO {
	// Database fields
	private SQLiteDatabase database;
	private MyDatabaseHelper dbHelper;

	public CitiesDAO(Context context) {
		dbHelper = new MyDatabaseHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public SQLiteDatabase getDatabase() {
		return this.database;
	}
	
	public long insertRow(CityTimeZone ctz) {
		return dbHelper.insertRow(ctz);
	}
	
	public List<CityTimeZone> getAllCities() {
		List<CityTimeZone> cities = new ArrayList<CityTimeZone>();
		
		Cursor cursor = database.query(MyDatabaseHelper.TABLE_CITIES_FTS,	//db name 
				new String[] {COL_KEY_CITY.getName(), COL_KEY_COUNTRY.getName(), COL_TIMEZONE.getName(), COL_TIMEZONE_NAME.getName(), COL_LATITUDE.getName(), COL_LONGITUDE.getName(), COL_CITY_PREF.getName(), COL_ID.getName()},	//columns
				COL_KEY_CITY.getName() + " MATCH ?",	//selection,
				new String[] { appendWildcard("v") + " " + COL_KEY_CITY + ": " + "" },		 //selectionArgs, 
				null,//groupBy, 
				null,//having, 
				null);//orderBy)
		
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			CityTimeZone comment = cursorToCityTimeZone(cursor);
			cities.add(comment);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		
		return cities;
	}
	
	 private String appendWildcard(String query) {
	        if (TextUtils.isEmpty(query)) return query;
	 
	        final StringBuilder builder = new StringBuilder();
	        final String[] splits = TextUtils.split(query, " ");
	 
	        for (String split : splits)
	          builder.append(split).append("*").append(" ");
	 
	        return builder.toString().trim();
	    }
	
	private CityTimeZone cursorToCityTimeZone(Cursor cursor) {
		CityTimeZone ctz = new CityTimeZone();
		ctz.setId(cursor.getString(1));
		ctz.setCity(cursor.getString(2));
		ctz.setCountry(cursor.getString(3));
		ctz.setTimezone(cursor.getString(4));
		ctz.setTimezoneName(cursor.getString(5));
		ctz.setLatitude(cursor.getDouble(6));
		ctz.setLongitude(cursor.getDouble(7));
		
		return ctz;
	}
}
