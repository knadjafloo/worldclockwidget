package com.threebars.worldclock;

import java.util.List;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class WorldClockWidgetActivity extends Activity {
	
	private static final String TAG = "WorldClockWidgetActivity";
	
	TextView searchTextView;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        searchTextView = (TextView)findViewById(R.id.searchText);
        
        Button searchButton = (Button)findViewById(R.id.okSearch);
        searchButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String searchedAddress = searchTextView.getText().toString();
				getLatitudeAndLongitudeFromGoogleMapForAddress(searchedAddress);
			}
		});
        
        TimeZoneParser parser = new TimeZoneParser(this);
        parser.parseFile();
    }
    
    public boolean getLatitudeAndLongitudeFromGoogleMapForAddress(String searchedAddress)
	{
	
	    Geocoder coder = new Geocoder(this);
	    List<Address> address;
	    try 
	    {
	        address = coder.getFromLocationName(searchedAddress,5);
	        if (address == null) {
	            Log.d(TAG, "############Address not correct #########");
	        }
	        Address location = address.get(0);
	
	        String message = "Address Latitude : "+ location.getLatitude() + "Address Longitude : "+ location.getLongitude();
	        Log.d(TAG, message);
	        Toast.makeText(this, message, Toast.LENGTH_LONG);
	        return true;
	
	    }
	    catch(Exception e)
	    {
	        Log.d(TAG, "MY_ERROR : ############Address Not Found");
	        return false;
	    }
	}
}