package com.example.livesearch;

import android.location.Location;
import android.location.LocationManager;

/**
 * Provides position data
 * 
 * @author Dmytro Khmelenko
 * 
 */
public class Position {

	private final String name;
	private final Location location;

	/**
	 * Constructor
	 * 
	 * @param aName
	 *            Position name
	 * @param aLatitude
	 *            Latitude
	 * @param aLongitude
	 *            Longitude
	 */
	public Position(String aName, double aLatitude, double aLongitude) {
		name = aName;

		// no matter which location provider is used
		location = new Location(LocationManager.NETWORK_PROVIDER);
		location.setLatitude(aLatitude);
		location.setLongitude(aLongitude);
	}

	/**
	 * Gets position name
	 * 
	 * @return Position name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets position location
	 * 
	 * @return Location
	 */
	public Location getLocation() {
		// provide immutability of location
		return new Location(location);
	}

}
