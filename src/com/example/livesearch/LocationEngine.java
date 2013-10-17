package com.example.livesearch;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

/**
 * Provides methods for getting user location
 * 
 * @author Dmytro Khmelenko
 * 
 */
public class LocationEngine {

	/** Location manager for retrieving location */
	private LocationManager locationManager;

	/** Contains last retrieved location */
	private Location lastLocation;

	/**
	 * Constructor
	 * 
	 * @param aContext
	 *            App context
	 */
	public LocationEngine(Context aContext) {
		locationManager = (LocationManager) aContext
				.getSystemService(Context.LOCATION_SERVICE);

		// update last location, could be null
		lastLocation = locationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	}

	/**
	 * Gets the last retrieved location
	 * 
	 * @return Location or null, if it's not existed
	 */
	public Location getLastLocation() {
		return lastLocation;
	}
}
