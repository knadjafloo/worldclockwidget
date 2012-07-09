package com.threebars.worldclock.dao;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.threebars.worldclock.CityTimeZone;
import com.threebars.worldclock.R;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.threebars.worldclock.dao.MyDatabaseHelper.COLUMN_NAMES.*;

public class MyDatabaseHelper extends SQLiteOpenHelper {

	public static enum COLUMN_NAMES {

		COL_ID("_id", 0),
		COL_KEY_CITY( "_city", 1),
		COL_KEY_COUNTRY("_country", 2),
		COL_TIMEZONE("_timezone", 3),
		COL_TIMEZONE_NAME("_tz_name", 4),
		COL_LATITUDE("_latitude", 5),
		COL_LONGITUDE("_longitude", 6),
		COL_CITY_PREF("_pref_name", 7);
		
		private String columnName;
		private int columnIndex;

		COLUMN_NAMES(String columnName, int columnIndex) {
			this.columnName = columnName;
			this.columnIndex = columnIndex;
		}
		
		public int getIndex() {
			return this.columnIndex;
		}
		
		public String getName() {
			return this.columnName;
		}
		
		public String toString() {
			return this.columnName;
		}

	};
	
	private SQLiteDatabase mDatabase;
	 
	public static final String TABLE_CITIES_FTS = "cities";
	private static final int DATABASE_VERSION = 1;
	
	private static final String TAG = "MyDatabaseHelper";
	
	private static final String DATABASE_CREATE = "CREATE VIRTUAL TABLE " + TABLE_CITIES_FTS + " USING fts3(" + COLUMN_NAMES.COL_ID + ", " + COL_KEY_CITY + ", "
            + COL_KEY_COUNTRY + ", "
            + COL_TIMEZONE + ", "
            + COL_TIMEZONE_NAME + ", "
            + COL_LATITUDE + ", "
            + COL_LONGITUDE + ", "
            + COL_CITY_PREF + ");";

	private final Context context;
	
	public MyDatabaseHelper(Context context) {
		super(context, TABLE_CITIES_FTS, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
		mDatabase = db;
		
//		loadCities();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(MyDatabaseHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CITIES_FTS);
		onCreate(db);
		
	}
	
	
    /**
     * Add a word to the dictionary.
     * @return rowId or -1 if failed
     */
    public long insertRow(CityTimeZone ctz) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(COL_KEY_CITY.getName(), ctz.city);
        initialValues.put(COL_KEY_COUNTRY.getName(), ctz.country);
        initialValues.put(COL_TIMEZONE.getName(), ctz.timezone);
        initialValues.put(COL_TIMEZONE_NAME.getName(), ctz.timezoneName);
        initialValues.put(COL_LATITUDE.getName(), ctz.latitude);
        initialValues.put(COL_LONGITUDE.getName(), ctz.longitude);
//        initialValues.put(COL_CITY_PREF.getName(), "-1");

        return mDatabase.insert(TABLE_CITIES_FTS, null, initialValues);
    }
    
    /**
     * Starts a thread to load the database table with words
     */
    private void loadCities() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    loadCitiesFromFile();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
    
    
    public List<CityTimeZone> parseFile() {
		BufferedReader br = null;
		List<CityTimeZone> cityTimeZoneList = new ArrayList<CityTimeZone>();
		try {

			final Resources resources = context.getResources();
			InputStream fis = resources.openRawResource(R.raw.cities);

			br = new BufferedReader(new InputStreamReader(fis));

			String line = null;
			while ((line = br.readLine()) != null) {

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
				int startBracketIndex = tokens[1].indexOf("(") + 1;
				int closeBracketIndex = tokens[1].indexOf(")");
				cityTimeZone.timezone = tokens[1].substring(startBracketIndex, closeBracketIndex);
				cityTimeZone.latitude = Double.parseDouble(tokens[3]);
				cityTimeZone.longitude = Double.parseDouble(tokens[4]);
				cityTimeZone.timezoneName = tokens[5];
					cityTimeZoneList .add(cityTimeZone);
//				System.out.println(cityTimeZone.toString());

			}
		} catch (FileNotFoundException fnf) {
			fnf.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}
		return cityTimeZoneList;
	}
    
    /**
     * read cities from text file and load them into db and fts db
     * @throws IOException
     */
    private void loadCitiesFromFile() throws IOException {
        Log.d(TAG, "Loading cities...");
        final Resources resources = context.getResources();
        InputStream inputStream = resources.openRawResource(R.raw.cities);
        
        long start = System.currentTimeMillis();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        
//    	InsertHelper ihFts = new InsertHelper(getWritableDatabase(), TABLE_CITIES_FTS);
//    	SQLiteDatabase db = getWritableDatabase();
    	
        try {
            
        	// Create a single InsertHelper to handle this set of insertions.
            
//            final int cityColumnFts = ihFts.getColumnIndex(COL_KEY_CITY.getName());
//            final int countryColumnFts = ihFts.getColumnIndex(COL_KEY_COUNTRY.getName());
//            final int tzColumnFts = ihFts.getColumnIndex(COL_TIMEZONE.getName());
//            final int tznColumnFts = ihFts.getColumnIndex(COL_TIMEZONE_NAME.getName());
//            final int latColumnFts = ihFts.getColumnIndex(COL_LATITUDE.getName());
//            final int longColumnFts = ihFts.getColumnIndex(COL_LONGITUDE.getName());
//            final int prefNColumnFts = ihFts.getColumnIndex(COL_CITY_PREF);
            
            
        	mDatabase.beginTransaction();
            
        	
            String line = null;
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
//				cityTimeZone.preferredName = "";
				// Get the InsertHelper ready to insert a single row
//                ihFts.prepareForInsert();
                
                
//                ihFts.bind(cityColumnFts, cityTimeZone.city);
//                ihFts.bind(countryColumnFts, cityTimeZone.country);
//                ihFts.bind(tzColumnFts, cityTimeZone.timezone);
//                ihFts.bind(tznColumnFts, cityTimeZone.timezoneName);
//                ihFts.bind(latColumnFts, cityTimeZone.latitude);
//                ihFts.bind(longColumnFts, cityTimeZone.longitude);
//                
//                
//                //	Insert the row into the database.
//                ihFts.execute();
				
                long id = insertRow(cityTimeZone);
                if (id < 0) {
                      Log.e(TAG, "unable to add city: " + cityTimeZone);
                }
			}
			
			mDatabase.setTransactionSuccessful();
        } finally {
            reader.close();
//            ih.close();
//            ihFts.close();
            mDatabase.endTransaction();
//            mDatabase.close();
        }
        
        long end = System.currentTimeMillis();
        
        Log.d(TAG, "TIME IT TOOK : " + (end - start) / 1000d + " seconds..." );
        Log.d(TAG, "DONE loading words.");
    }
}
