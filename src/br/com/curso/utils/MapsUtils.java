package br.com.curso.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsUtils {

	public static void turnGPSOn(Context context){
		Intent intent=new Intent("android.location.GPS_ENABLED_CHANGE");
		intent.putExtra("enabled", true);
		context.sendBroadcast(intent);
	}

	public static void turnGPSOff(Context context){
		Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
		intent.putExtra("enabled", false);
		context.sendBroadcast(intent);
	}
	
	public static void addMarker(GoogleMap map, LatLng point,
			String title, String body, BitmapDescriptor icon) {		

		MarkerOptions options = null;
		
		if(icon == null){
			options = new MarkerOptions()
					.position(point)
					.title(title)
					.snippet(body);
			
		} else{
			options = new MarkerOptions()
	        	.position(point)
	        	.title(title)
	        	.snippet(body)
	        	.icon(icon);
			
			//BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
		}
		
		map.addMarker(options);		
	}
	
	public static void drawRouteMap(LatLng src, LatLng dest, int colorRoute, GoogleMap mapView, List<String> route) {
		ArrayList<LatLng> points = new ArrayList<LatLng>();
		
		for(int i=1; i < route.size(); i++){ // adiciona a rota no mapa
			String[] coordenada = route.get(i).split(",");
			points.add(getLatLng(coordenada));
		} 
		
		//adiciona o ponto inicial no mapa
		mapView.addMarker(new MarkerOptions()
		        .position(src)
		        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
			
		//adiciona a rota no mapa
		mapView.addPolyline(new PolylineOptions()
				.addAll(points)
				.visible(true)
				.color(colorRoute)
				);
		
		//adiciona o ponto final no mapa
		mapView.addMarker(new MarkerOptions()
		        .position(src)
		        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
	}
	
	public static LatLng getLatLng(String[] coordenada){
		//modelo oordenada[]: -38.484310,-3.831110,0.000000
		double longitude = Double.parseDouble(coordenada[0]);
		double latitude = Double.parseDouble(coordenada[1]);
		String height = coordenada[2];
		
		LatLng ponto = new LatLng(latitude, longitude);
		return ponto;
	}
	
	/**
	 * Obtem o endereco a partir da latitude e longitude
	 * 
	 * @param lat
	 * @param lng
	 * @return JSON
	 */
	public static JSONObject getLocationInfo(String url) {

		HttpGet httpGet = new HttpGet(url);
		
		HttpClient client = new DefaultHttpClient();
		HttpResponse response;
		StringBuilder stringBuilder = new StringBuilder();

		try {
			response = client.execute(httpGet);
			HttpEntity entity = response.getEntity();
			InputStream stream = entity.getContent();
			int b;
			while ((b = stream.read()) != -1) {
				stringBuilder.append((char) b);
			}
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		}

		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject = new JSONObject(stringBuilder.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return jsonObject;
	}

	/**
	 * Obtem o endereco a partir da latitude e longitude
	 * 
	 * @param lat
	 * @param lng
	 * @return String
	 */
	public static String getCurrentLocationViaJSON(double lat, double lng) {

		JSONObject jsonObj = getLocationInfo(
				"http://maps.googleapis.com/maps/api/geocode/json?latlng="
						+ lat + "," + lng + "&sensor=false");
		
		Log.i("maps", jsonObj.toString());

		String currentLocation = "testing";
		String street_address = null;
		String postal_code = null;

		try {
			String status = jsonObj.getString("status").toString();
			Log.i("status", status);

			if (status.equalsIgnoreCase("OK")) {
				JSONArray results = jsonObj.getJSONArray("results");
				int i = 0;
				Log.i("i", i + "," + results.length()); // TODO delete this
				do {

					JSONObject r = results.getJSONObject(i);
					JSONArray typesArray = r.getJSONArray("types");
					String types = typesArray.getString(0);

					if (types.equalsIgnoreCase("street_address")) {
						street_address = r.getString("formatted_address")
								.split(",")[0];
						Log.i("street_address", street_address);
						
					} else if (types.equalsIgnoreCase("postal_code")) {
						postal_code = r.getString("formatted_address");
						Log.i("postal_code", postal_code);
					}

					if (street_address != null && postal_code != null) {
						currentLocation = street_address + "," + postal_code;
						Log.i("Current Location =>", currentLocation); // Delete
																		// this
						i = results.length();
					}

					i++;
				} while (i < results.length());

				Log.i("maps", currentLocation);
				return currentLocation;
			}

		} catch (JSONException e) {
			Log.e("maps", "Failed to load JSON");
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getGoogleMapsUrl() {		
		return "http://maps.googleapis.com/maps/api/geocode/json";
	}
	
	public static String addressToGoogleMaps(String address) {
		address = address.trim().replace(" ", "+");
		return address;
	}
	
	public static LatLng getCurrentLocationViaJSON(String jsonStr) {
//		address = address.trim().replace(" ", "+");
//		
//		JSONObject jsonObj = getLocationInfo(
//				"http://maps.googleapis.com/maps/api/geocode/json?address="
//						+ address + "&sensor=false");
				
		try {
			JSONObject jsonObj = new JSONObject(jsonStr);
//			Log.i("maps", jsonObj.toString());
			
			String status = jsonObj.getString("status").toString();
//			Log.i("maps", status);

			if (status.equalsIgnoreCase("OK")) {
				JSONArray results = jsonObj.getJSONArray("results");
				int i = 0;
				
//				do {
					JSONObject r = results.getJSONObject(i);

					double lng = r
				            .getJSONObject("geometry").getJSONObject("location")
				            .getDouble("lng");

					double lat = r
				            .getJSONObject("geometry").getJSONObject("location")
				            .getDouble("lat");
					
//					i++;
//				} while (i < results.length());

				return new LatLng(lat, lng);
			}

		} catch (JSONException e) {
			Log.e("maps", "Failed to load JSON");
			e.printStackTrace();
		}
		return null;
	}

	public static String getAddress(Context context, int latitude, int longitude) {
		try {
			Geocoder geocoder;
			List<Address> addresses;
			geocoder = new Geocoder(context);
			
			if (latitude != 0 || longitude != 0) {
				addresses = geocoder.getFromLocation(latitude, longitude, 1);
				
				if(addresses.size() > 0){
					String address = addresses.get(0).getAddressLine(0);
					String city = addresses.get(0).getAddressLine(1);
					String country = addresses.get(0).getAddressLine(2);
					
					Log.d("maps", "address = " + address + ", city =" + city
							+ ", country = " + country);
					
					return address + ", " + city + " - " + country;
				}
				
			} else {
				return null;
			}
			
		} catch (Exception e) {
			Log.e("maps", e.getMessage(), e);
			return null;
		}
		
		return null;
	}

	public static LatLng getAddress(Context context, String address) {
		try {
			Geocoder geocoder;
			List<Address> addresses;
			geocoder = new Geocoder(context);
			
			if (address != null && !address.equals("")) {
				addresses = geocoder.getFromLocationName(address, 1);
				
				if(addresses.size() > 0){
					double latitude = addresses.get(0).getLatitude();
					double longitude = addresses.get(0).getLongitude();
					
					Log.d("maps", "lat = " + latitude + ", longitude =" + longitude);
					
					
					LatLng latLng = new LatLng(latitude, longitude);
					return latLng;
				}
				
			} else {
				return null;
			}
			
		} catch (Exception e) {
			Log.e("maps", e.getMessage(), e);
			return null;
		}
		
		return null;
	}
}
