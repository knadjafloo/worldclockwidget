package com.threebars.worldclock;

public class CityTimeZone {

	public String city;
	public String country;
	public double longitude;
	public double latitude;
	public String timezone;
	public String timezoneName;
	public String preferredName;

	@Override
	public String toString() {
		return city + ", " + country + ", " + latitude + ", " + longitude + ", " + timezone + ", " + timezoneName + ", " + preferredName;
	}
}
