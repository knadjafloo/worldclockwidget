package com.threebars.worldclock;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DigitalClock;
import android.widget.TextView;



public class IconicAdapter extends ArrayAdapter<CityTimeZone> {
	
	private List<CityTimeZone> data;
	private Context context;

	public IconicAdapter(List<CityTimeZone> cities, Context context) {
		super(context, R.layout.row2, cities);
		this.data = cities;
		this.context = context;
	}
	
	

	public void setData(List<CityTimeZone> array) {
		data = array;
		System.err.println( "#### size of data : " + data.size());
	}
	
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;

		if (row == null) {
//			LayoutInflater inflater = getLayoutInflater();
			LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

			row = inflater.inflate(R.layout.row2, parent, false);
		}

		TextView label = (TextView) row.findViewById(R.id.label);
		CityTimeZone cityTimeZone = data.get(position);
		label.setText(cityTimeZone.city);
		
		DigitalClock clock = (DigitalClock) row.findViewById(R.id.clock);
		
		TextView country  = (TextView) row.findViewById(R.id.country);
		country.setText(cityTimeZone.country);
		
		TextView gmt = (TextView) row.findViewById(R.id.gmt);
		gmt.setText(cityTimeZone.timezone + " " + cityTimeZone.timezoneName);
		
//		label.setText(array.get(position));

		return (row);
	}
}