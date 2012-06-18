package com.threebars.worldclock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DigitalClock;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.commonsware.cwac.tlv.TouchListView;

public class WorldClockWidgetActivity extends SherlockListActivity {
	
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
			CityTimeZone item = adapter.getItem(from);
			

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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSherlock().getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		
		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
	    // Configure the search info and add any event listeners
		
		return true;
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main);
//        
//        searchTextView = (TextView)findViewById(R.id.searchText);
//        
//        Button searchButton = (Button)findViewById(R.id.okSearch);
//        searchButton.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				String searchedAddress = searchTextView.getText().toString();
//				getLatitudeAndLongitudeFromGoogleMapForAddress(searchedAddress);
//			}
//		});
        
 
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        
        CustomDigitalClock digitalClock = new CustomDigitalClock(this);
        digitalClock.setTextSize(45f);
//        Typeface tf = Typeface.createFromAsset(getAssets(),
//                "fonts/Y2K.ttf");
//        digitalClock.setTypeface(tf);
//        setContentView(digitalClock);
    	
    	setContentView(R.layout.main_list);

		TouchListView tlv=(TouchListView)getListView();
		
		TimeZoneParser parser = new TimeZoneParser(WorldClockWidgetActivity.this);
        parser.parseFile();
        List<CityTimeZone> cities = parser.getAllCities();
		
		adapter=new IconicAdapter(cities, this);
		setListAdapter(adapter);
		
//		ParseFileTask parserTask = new ParseFileTask();
//		parserTask.execute(adapter);
		
		tlv.setDropListener(onDrop);
		tlv.setRemoveListener(onRemove);
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
    
    private class ParseFileTask extends AsyncTask<IconicAdapter, Void, List<CityTimeZone>>
    {

    	private IconicAdapter adapter;
		@Override
		protected List<CityTimeZone> doInBackground(IconicAdapter... params) {
			adapter = params[0];
			TimeZoneParser parser = new TimeZoneParser(WorldClockWidgetActivity.this);
	        parser.parseFile();
	        List<CityTimeZone> cities = parser.getAllCities();
	        adapter.setData(cities);
			for(CityTimeZone item : cities)
				adapter.add(item);
			
			System.err.println(" # of cities : " + cities.size());
			return cities;
		}
		
		@Override
		protected void onPostExecute(List<CityTimeZone> result) {
			
			adapter.notifyDataSetChanged();
		}
    	
    }
    
	
	public ActionBar getSupportActionBar() {
		return getSherlock().getActionBar();
	}
	
}