package com.threebars.worldclock;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DigitalClock;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.commonsware.cwac.tlv.TouchListView;
import com.threebars.worldclock.dao.CitiesDAO;

public class WorldClockWidgetActivity extends SherlockListActivity {
	
	private static String[] items={"lorem", "ipsum", "dolor", "sit", "amet",
		"consectetuer", "adipiscing", "elit", "morbi", "vel",
		"ligula", "vitae", "arcu", "aliquet", "mollis",
		"etiam", "vel", "erat", "placerat", "ante",
		"porttitor", "sodales", "pellentesque", "augue", "purus"};
	
	private IconicAdapter adapter=null;
	private ArrayList<String> array=new ArrayList<String>(Arrays.asList(items));
	private static final String TAG = "WorldClockWidgetActivity";
	
	private ProgressDialog progressBar;
	
//	private OnQueryTextListener onQueryTextListener = new OnQueryTextListener() {
//		
//		@Override
//		public boolean onQueryTextSubmit(String query) {
//			// TODO Auto-generated method stub
//			return false;
//		}
//		
//		@Override
//		public boolean onQueryTextChange(String newText) {
//			// TODO Auto-generated method stub
//			return false;
//		}
//	};
	
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
		
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	    	
	        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
		    // Configure the search info and add any event listeners
	        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
	        searchView.setIconifiedByDefault(false);
	        
//	        MenuItem menuSearch = (MenuItem)findViewById(R.id.menu_search);
//	        menuSearch.setActionView(searchView);	//programmatically setActionView to be backwards compatible on older devices
	    }
	    return true;
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.menu_search:
	            onSearchRequested();
	            Toast.makeText(this, "search invoked..", Toast.LENGTH_SHORT);
	            return true;
	        default:
	            return false;
	    }
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
        
        //if it's the first time we're running the app, load all the cities
        
        CitiesDAO dao = new CitiesDAO(this);
//        dao.open();
        List<CityTimeZone> cities = new ArrayList<CityTimeZone>();// dao.getAllCities();
//        dao.close();
        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);
        
        
//        CustomDigitalClock digitalClock = new CustomDigitalClock(this);
//        digitalClock.setTextSize(45f);
//        Typeface tf = Typeface.createFromAsset(getAssets(),
//                "fonts/Y2K.ttf");
//        digitalClock.setTypeface(tf);
//        setContentView(digitalClock);
    	
    	setContentView(R.layout.main_list);

		TouchListView tlv=(TouchListView)getListView();
		
//		TimeZoneParser parser = new TimeZoneParser(WorldClockWidgetActivity.this);
//        parser.parseFile();
//        List<CityTimeZone> cities = parser.getAllCities();
//		
		adapter=new IconicAdapter(cities, this);
		setListAdapter(adapter);
		
//		ParseFileTask parserTask = new ParseFileTask();
//		parserTask.execute(adapter);
		
		tlv.setDropListener(onDrop);
		tlv.setRemoveListener(onRemove);
		
		 SharedPreferences prefs = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
	        boolean haveWeShownPreferences = prefs.getBoolean("hasLoadedCities", false);

	        if (!haveWeShownPreferences) {
	            // launch the preferences activity
	        	
	        	

	        	
	        	new LoadCitiesTask().execute();
	        	
	        	SharedPreferences.Editor ed = prefs.edit();
	        	ed.putBoolean("hasLoadedCities", true);
	        	ed.commit();  //Commiting changes
	        } else {
	           // we have already shown the preferences activity before
	        }
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
    
    private class LoadCitiesTask extends AsyncTask<Void, Integer,Void> {

    	private int maxLineNumber = 100;
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    		try {
	    		final Resources resources = getResources();
				InputStream inputStream = resources.openRawResource(R.raw.cities);
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				
	    		LineNumberReader  lnr = new LineNumberReader(reader);
				lnr.skip(Long.MAX_VALUE);
				maxLineNumber = lnr.getLineNumber();
	
				progressBar = new ProgressDialog(WorldClockWidgetActivity.this);
	        	progressBar.setMessage("Loading Cities for first time...");
	        	progressBar.setCancelable(false);
	        	progressBar.setIndeterminate(false);
	        	progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				
				progressBar.setMax(maxLineNumber);
	    		progressBar.show();
    		
				reader.close();
				inputStream.close();
			} catch (IOException e) {
			}
    	}
		@Override
		protected Void doInBackground(Void... params) {
			Log.d(TAG, "Loading cities...");
			final Resources resources = getResources();
			InputStream inputStream = resources.openRawResource(R.raw.cities);
			
			CitiesDAO dao = new CitiesDAO(WorldClockWidgetActivity.this);
			dao.open();
			
			SQLiteDatabase mDatabase = dao.getDatabase();

			long start = System.currentTimeMillis();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			
			try {
				
				mDatabase.beginTransaction();

	            String line = null;
	            int count = 0;
				try {
					while ((line = reader.readLine()) != null) {
						
						CityTimeZone cityTimeZone = new CityTimeZone();
						
						String otherThanQuote = " [^\"] ";
					    String quotedString = String.format(" \" %s* \" ", otherThanQuote);
					    String regex = String.format("(?x) "+ // enable comments, ignore white spaces
					            ",                         "+ // match a comma
					            "(?=                       "+ // start positive look ahead
					            "  (                       "+ //   start group 1
					            "    %s*                   "+ //     match 'otherThanQuote' zero or more times
					            "    %s                    "+ //     match 'quotedString'
					            "  )*                      "+ //   end group 1 and repeat it zero or more times
					            "  %s*                     "+ //   match 'otherThanQuote'
					            "  $                       "+ // match the end of the string
					            ")                         ", // stop positive look ahead
					            otherThanQuote, quotedString, otherThanQuote);

						String[] tokens = line.split(regex);
	
						cityTimeZone.city = tokens[0];
						cityTimeZone.country = tokens[2];
						cityTimeZone.timezone = tokens[1].substring(2, 11);
						cityTimeZone.latitude = Double.parseDouble(tokens[3]);
						cityTimeZone.longitude = Double.parseDouble(tokens[4]);
						cityTimeZone.timezoneName = tokens[5];
						// cityTimeZone.preferredName = "";
	
						long id = dao.insertRow(cityTimeZone);
						if (id < 0) {
							Log.e(TAG, "unable to add city: " + cityTimeZone);
						}
						count++;
						publishProgress((int) ((count / (float) maxLineNumber) * 100));
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				mDatabase.setTransactionSuccessful();
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
				}
				mDatabase.endTransaction();
			}
			dao.close();
			long end = System.currentTimeMillis();

			Log.d(TAG, "TIME IT TOOK : " + (end - start) / 1000d + " seconds...");
			Log.d(TAG, "DONE loading words.");
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			progressBar.setProgress(progress[0]);
		}

		@Override
		protected void onPostExecute(Void result) {
			progressBar.dismiss();
		}
	     
    	
    }
    
    
	
	public ActionBar getSupportActionBar() {
		return getSherlock().getActionBar();
	}
	
}