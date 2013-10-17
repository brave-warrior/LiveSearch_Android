package com.example.livesearch;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

/**
 * Main screen
 * 
 * @author Dmytro Khmelenko
 * 
 */
public class MainActivity extends Activity {

	// editors
	private AutoCompleteTextView departureEditor;
	private AutoCompleteTextView arrivalEditor;

	// buttons
	private Button searchBtn;
	private Button dateBtn;

	private Calendar todayCalendar = Calendar.getInstance();

	/** Provides work with the location */
	private LocationEngine locationEngine;

	/*
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		locationEngine = new LocationEngine(this);

		initDateBtn();
		initSearchBtn();

		initDepartureEditor();
		initArrivalEditor();

		updateSearchAvailability();
	}

	/**
	 * Initializes date button
	 */
	private void initDateBtn() {
		dateBtn = (Button) findViewById(R.id.date_btn);
		updateDate(todayCalendar);

		dateBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View aV) {
				showDatePicker();
			}
		});
	}

	/**
	 * Shows date picker dialog
	 */
	private void showDatePicker() {
		int year = todayCalendar.get(Calendar.YEAR);
		int month = todayCalendar.get(Calendar.MONTH);
		int day = todayCalendar.get(Calendar.DAY_OF_MONTH);

		DatePickerDialog dialog = new DatePickerDialog(this,
				new OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker aView, int aYear,
							int aMonthOfYear, int aDayOfMonth) {
						todayCalendar.set(Calendar.YEAR, aYear);
						todayCalendar.set(Calendar.MONTH, aMonthOfYear);
						todayCalendar.set(Calendar.DAY_OF_MONTH, aDayOfMonth);

						updateDate(todayCalendar);
					}
				}, year, month, day);

		dialog.show();
	}

	/**
	 * Updates the date on the date button
	 * 
	 * @param aDate
	 *            Date for showing
	 */
	private void updateDate(Calendar aDate) {
		String date = getCurrentTimeString(aDate.getTimeInMillis());
		dateBtn.setText(date);
	}

	/**
	 * Gets formatted string of the date in milliseconds
	 * 
	 * @param aDate
	 *            Date in milliseconds
	 * @return Formatted string
	 */
	@SuppressLint("SimpleDateFormat")
	public String getCurrentTimeString(long aDate) {
		String pattern = "dd.MM.yyyy";
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		String result = formatter.format(aDate);
		return result;
	}

	/**
	 * Initializes button Seach
	 */
	private void initSearchBtn() {
		searchBtn = (Button) findViewById(R.id.search_btn);

		searchBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View aV) {
				String message = getResources().getString(
						R.string.msg_search_not_implemented);
				Toast.makeText(getApplicationContext(), message,
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * Updates the availability of the search button
	 */
	private void updateSearchAvailability() {
		String departure = departureEditor.getText().toString();
		String arrival = arrivalEditor.getText().toString();
		boolean searchEnabled = !departure.isEmpty() && !arrival.isEmpty();
		searchBtn.setEnabled(searchEnabled);
	}

	/**
	 * Initializes editor for the input departure place
	 */
	private void initDepartureEditor() {
		departureEditor = (AutoCompleteTextView) findViewById(R.id.departure);
		departureEditor.setAdapter(new CustomAdapter(this,
				android.R.layout.simple_dropdown_item_1line));
	}

	/**
	 * Initializes editor for the input arrival place
	 */
	private void initArrivalEditor() {
		arrivalEditor = (AutoCompleteTextView) findViewById(R.id.arrival);
		arrivalEditor.setAdapter(new CustomAdapter(this,
				android.R.layout.simple_dropdown_item_1line));
	}

	/**
	 * Custom adapter for the {@link AutoCompleteTextView}
	 * 
	 * @author Dmytro Khmelenko
	 * 
	 */
	private class CustomAdapter extends ArrayAdapter<String> implements
			Filterable {

		/** List of results */
		private List<String> resultList;

		/**
		 * Constructor
		 * 
		 * @param aContext
		 *            Context
		 * @param aTextViewResourceId
		 */
		public CustomAdapter(Context aContext, int aTextViewResourceId) {
			super(aContext, aTextViewResourceId);
		}

		@Override
		public int getCount() {
			return resultList.size();
		}

		@Override
		public String getItem(int aIndex) {
			return resultList.get(aIndex);
		}

		@Override
		public Filter getFilter() {
			Filter filter = new Filter() {
				@Override
				protected FilterResults performFiltering(
						CharSequence aConstraint) {
					FilterResults filterResults = new FilterResults();
					if (aConstraint != null) {

						// make request to the serve and parse the results
						String result = doRequest(aConstraint.toString());
						List<Position> items = NetworkEngine
								.parsePositions(result);
						
						// sort positions
						sortPositions(items);

						// create the list of the names of the positions
						ArrayList<String> list = new ArrayList<String>();
						for (Position pos : items) {
							list.add(pos.getName());
						}

						resultList = list;
						// Assign the data to the FilterResults
						filterResults.values = resultList;
						filterResults.count = resultList.size();
					}

					return filterResults;
				}

				@Override
				protected void publishResults(CharSequence constraint,
						FilterResults results) {
					if (results != null && results.count > 0) {
						notifyDataSetChanged();
					} else {
						notifyDataSetInvalidated();
					}
					
					updateSearchAvailability();
				}
			};
			return filter;
		}
	}

	/**
	 * Sorts the position by the distance to the user location
	 */
	private void sortPositions(List<Position> aItems) {
		Collections.sort(aItems, new Comparator<Position>() {

			@Override
			public int compare(Position pos1, Position pos2) {
				// compare the distances to the user location
				Location userLocation = locationEngine.getLastLocation();
				if (userLocation == null) {
					return 0;
				}
				float dist1 = userLocation.distanceTo(pos1.getLocation());
				float dist2 = userLocation.distanceTo(pos2.getLocation());
				return (int) (dist1 - dist2);
			}
		});
	}

	/**
	 * Makes request to the server with a aText
	 * 
	 * @param aText
	 *            Text for request
	 * @return Server response
	 */
	private String doRequest(String aText) {
		String result = "";

		if (NetworkEngine.isNetworkAvailable(getApplicationContext())) {
			// setting connection timeout
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters,
					NetworkEngine.CONNECTION_TIMEOUT);
			HttpConnectionParams.setSoTimeout(httpParameters,
					NetworkEngine.CONNECTION_TIMEOUT);
			// Create a new HttpClient
			HttpClient httpclient = new DefaultHttpClient(httpParameters);

			try {
				// Execute HTTP Request
				URL url = new URL(NetworkEngine.CONNECTION_URL + aText);

				HttpGet httpget = new HttpGet(url.toString());
				httpget.setHeader("Content-Type", "application/json");
				HttpResponse response = httpclient.execute(httpget);
				int statusCode = response.getStatusLine().getStatusCode();

				// check status code
				if (statusCode == HttpStatus.SC_OK) {
					HttpEntity entity = response.getEntity();
					String responseString = EntityUtils.toString(entity);
					result = responseString;
				} else {
					result = response.getStatusLine().getReasonPhrase();
				}

			} catch (ClientProtocolException e) {
				String ex = e.toString();
				result = ex;
			} catch (IOException e) {
				String ex = e.toString();
				result = ex;
			}
		}

		return result;
	}
}
