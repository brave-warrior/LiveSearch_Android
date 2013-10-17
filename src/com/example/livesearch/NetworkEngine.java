package com.example.livesearch;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkEngine {

	/** Network connection timeout */
	public static final int CONNECTION_TIMEOUT = 30000; // 30 seconds

	/** Connection URL for requests */
	public static final String CONNECTION_URL = "http://pre.dev.goeuro.de:12345/api/v1/suggest/position/en/name/";

	// keys
	private static final String KEY_RESULTS = "results";
	private static final String KEY_NAME = "name";
	private static final String KEY_GEO_POSITION = "geo_position";
	private static final String KEY_LATITUDE = "latitude";
	private static final String KEY_LONGITUDE = "longitude";

	/**
	 * Checks whether network is available or not
	 * 
	 * @return True is network is available. Otherwise, false
	 */
	public static boolean isNetworkAvailable(Context aContext) {
		ConnectivityManager cm = (ConnectivityManager) aContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		// if no network is available networkInfo will be null
		// otherwise check if we are connected
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}

	/**
	 * Parses positions from the received response
	 * 
	 * @param aResult
	 *            Response
	 * @return List of positions
	 */
	public static List<Position> parsePositions(String aResult) {
		List<Position> positionItems = new ArrayList<Position>();
		try {
			JSONObject holder = new JSONObject(aResult);

			JSONArray results = holder.getJSONArray(KEY_RESULTS);
			for (int i = 0; i < results.length(); i++) {
				JSONObject item = results.getJSONObject(i);
				String name = item.getString(KEY_NAME);
				
				JSONObject location = item.getJSONObject(KEY_GEO_POSITION);
				double latitude = location.getDouble(KEY_LATITUDE);
				double longitude = location.getDouble(KEY_LONGITUDE);

				Position position = new Position(name, latitude, longitude);
				positionItems.add(position);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return positionItems;
	}

}
