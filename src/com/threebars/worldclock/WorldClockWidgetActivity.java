package com.threebars.worldclock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.commonsware.cwac.tlv.TouchListView;

import android.app.Activity;
import android.app.ListActivity;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DigitalClock;
import android.widget.TextView;
import android.widget.Toast;

public class WorldClockWidgetActivity extends Activity {
	
	private static String[] items={"lorem", "ipsum", "dolor", "sit", "amet",
		"consectetuer", "adipiscing", "elit", "morbi", "vel",
		"ligula", "vitae", "arcu", "aliquet", "mollis",
		"etiam", "vel", "erat", "placerat", "ante",
		"porttitor", "sodales", "pellentesque", "augue", "purus"};
	
	private IconicAdapter adapter=null;
	private ArrayList<String> array=new ArrayList<String>(Arrays.asList(items));
	private static final String TAG = "WorldClockWidgetActivity";
	
	TextView searchTextView;
	
	private TouchListView.DropListener onDrop = new TouchListView.DropListener() {
		@Override
		public void drop(int from, int to) {
			String item = adapter.getItem(from);

			adapter.remove(item);
			adapter.insert(item, to);
		}
	};

	private TouchListView.RemoveListener onRemove = new TouchListView.RemoveListener() {
		@Override
		public void remove(int which) {
			adapter.remove(adapter.getItem(which));
		}
	};
	
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
        
//        TimeZoneParser parser = new TimeZoneParser(this);
//        parser.parseFile();
        
        CustomDigitalClock digitalClock = new CustomDigitalClock(this);
        digitalClock.setTextSize(45f);
//        Typeface tf = Typeface.createFromAsset(getAssets(),
//                "fonts/Y2K.ttf");
//        digitalClock.setTypeface(tf);
        setContentView(digitalClock);
    	
//    	setContentView(R.layout.main_list);
//
//		TouchListView tlv=(TouchListView)getListView();
//		adapter=new IconicAdapter();
//		setListAdapter(adapter);
//		
//		tlv.setDropListener(onDrop);
//		tlv.setRemoveListener(onRemove);
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
    
	class IconicAdapter extends ArrayAdapter<String> {
		IconicAdapter() {
			super(WorldClockWidgetActivity.this, R.layout.row2, array);
		}
		
		public View getView(int position, View convertView,
												ViewGroup parent) {
			View row=convertView;
			
			if (row==null) {													
				LayoutInflater inflater=getLayoutInflater();
				
				row=inflater.inflate(R.layout.row2, parent, false);
			}
			
			TextView label=(TextView)row.findViewById(R.id.label);
			
			label.setText(array.get(position));
			
			return(row);
		}
	}
}