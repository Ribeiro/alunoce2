package br.com.curso.view;

import roboguice.activity.RoboFragmentActivity;
import roboguice.inject.ContentView;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import br.com.curso.controller.HttpController;
import br.com.curso.model.Aluno;
import br.com.curso.utils.Constantes;
import br.com.curso.utils.MapsUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * http://hmkcode.com/getting-android-google-maps-v2-api-key/
 * http://hmkcode.com/run-google-map-v2-on-android-emulator/
 * http://hmkcode.com/adding-google-play-services-library-to-your-android-app/
 * http://hmkcode.com/android-google-map-v2-on-older-android-version/
 * 
 * @author gabriel
 *
 */
@ContentView(R.layout.activity_mapa)
public class MapaActivity extends RoboFragmentActivity implements LocationListener{

	private GoogleMap googleMap;
	private LocationManager locationManager;
	
	private String endereco;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Obtem o status do Google Play
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
 
        // Verifica o status
		if (status != ConnectionResult.SUCCESS) { // O Google Play Services nao esta disponivel
 
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();
 
        } else { // O Google Play Services esta disponivel
        
        	// Habilita a camada MyLocation do Google Maps
	        getGoogleMap().setMyLocationEnabled(true);
	
			// Configura o tipo do mapa
			setTipoMapa();
			
			// Habilita todas as funcionalidades visuais do Google Maps
			getGoogleMap().getUiSettings().setAllGesturesEnabled(true);
			
			Intent it = getIntent();
			if(it != null){
				try {
					Aluno aluno = it.getParcelableExtra("aluno");
					endereco = aluno.getEndereco().trim();
//					endereco = "1600 Amphitheatre Parkway, Mountain View, CA";
					
					RequestParams params = new RequestParams();
					params.put("sensor", MapsUtils.addressToGoogleMaps(endereco));
					params.put("sensor", "false");
					
					sendHttpGet(MapsUtils.getGoogleMapsUrl(), params);
					
				} catch (Exception e) {
					Log.e(Constantes.CATEGORIA, e.getMessage(), e);
				}
			}
        }
	}

    @Override
    protected void onResume() {
    	super.onResume();

		// Configura o tipo do mapa
		setTipoMapa();
    }
    
	@Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	// Remove o listener para nao ficar atualizando mesmo depois de sair
    	getLocationManager().removeUpdates(this);
    	
    	// Desabilita o gps
    	MapsUtils.turnGPSOff(getApplicationContext());
    }
    
	private LocationManager getLocationManager() {
		if(locationManager == null){
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}
		return locationManager;
	}
	
	private GoogleMap getGoogleMap(){
		if(googleMap == null){
			googleMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.mapa))
			        .getMap();
		}
		
		return googleMap;
	}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) {
    	MapsUtils.turnGPSOn(getApplicationContext());
    }

    @Override
    public void onLocationChanged(Location location) {
//    	Toast.makeText(this, "onLocationChanged: latitude: " + location.getLatitude() + " - longitude: " + location.getLongitude(),
//    			Toast.LENGTH_LONG).show();
        updateWithNewLocation(location);
    }
    
    public void updateWithNewLocation(Location location) {
		if(location != null){
//			Log.i(Constantes.LOGCAT, "onLocationChanged: latitude: " + location.getLatitude() + " - longitude: " + location.getLongitude());
						
			// Creating a LatLng object for the current location
	        LatLng coordenada = new LatLng(location.getLatitude(), location.getLongitude());			
			mapToLocation(coordenada);
		}
	}

	private void mapToLocation(LatLng latLng) {
		if(latLng != null){
			// Mostra a localizacao atual no Google Maps
	        getGoogleMap().moveCamera(CameraUpdateFactory.newLatLng(latLng));
	        
	        // COnfigura o zoom no Google Maps
	        getGoogleMap().animateCamera(CameraUpdateFactory.zoomTo(16));
		}
	}
    
    private void setTipoMapa() {
//    	String tipoMapa = "Satélite";
    	String tipoMapa = "Híbrido";
//    	String tipoMapa = "Mapa";
		
    	if(tipoMapa.equalsIgnoreCase("Satélite")){ // satelite
    		getGoogleMap().setMapType(GoogleMap.MAP_TYPE_SATELLITE);
    		
		} else if(tipoMapa.equalsIgnoreCase("Híbrido")){ // hibrido
    		getGoogleMap().setMapType(GoogleMap.MAP_TYPE_HYBRID);
			
		} else { // mapa
			getGoogleMap().setMapType(GoogleMap.MAP_TYPE_NORMAL);
			
		}
	}

	private void sendHttpGet(String servletName, RequestParams params) {		
		HttpController.get(servletName, params, new AsyncHttpResponseHandler() {
		    @Override
		    public void onSuccess(String response) {
		    	LatLng point = MapsUtils.getCurrentLocationViaJSON(response);
				
				if(point != null){
//					Log.i("maps", point.latitude + "," + point.longitude);
					String msg = endereco;
					MapsUtils.addMarker(getGoogleMap(), point, msg, msg, null);
					mapToLocation(point);
				}		    	
		    }
		});
	}

}