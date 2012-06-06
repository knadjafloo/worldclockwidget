package com.threebars.worldclock;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class TimeZoneParser {

	private final static String CITIES_FILE = "cities.txt";

	private Context context;

	public TimeZoneParser(Context context) {
		this.context = context;
	}

	public void parseFile() {
		BufferedReader br = null;
		try {

			AssetManager am = context.getAssets();
			InputStream is = am.open(CITIES_FILE);

			br = new BufferedReader(new InputStreamReader(is));

			
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
				cityTimeZone.timezone = tokens[1].substring(2, 11);
				cityTimeZone.latitude = Double.parseDouble(tokens[3]);
				cityTimeZone.longitude = Double.parseDouble(tokens[4]);
				cityTimeZone.timezoneName = tokens[5];
				
				System.out.println(cityTimeZone.toString());

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
	}
}

