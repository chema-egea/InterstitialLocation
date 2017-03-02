package chema.egea.canales.Interstitial_in_GPSLocation;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    // Remove the below line after defining your own ad unit ID.
    private static final String TOAST_TEXT = "Se mostrará un anuncio cuando estemos en la Universidad de Alicante";
    private final int LOCATION_PERMISO = 123;
    //Anuncio
    private InterstitialAd mInterstitialAd;

    //Text views latitud y longitud
    private TextView mLatitudTextView;
    private TextView mLongitudTextView;

    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //


        // ********************
        //  --   ANUNCIO   --
        // ********************
        // Create the InterstitialAd and set the adUnitId (defined in values/strings.xml).
        mInterstitialAd = newInterstitialAd();
        loadInterstitial();

        // ***********************************
        // PARA LOCALIZACION
        // ***********************************

        mLatitudTextView = (TextView) findViewById(R.id.latitud);
        mLongitudTextView = (TextView) findViewById(R.id.longitud);

        //Comprobamos permiso de localizacion
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISO);
            }
        } else {
            inicializarLocation();
        }


    }


    //BUTTON INFO IN THE VIEW
    public void DameToast(View view)
    {
        Toast.makeText(this, TOAST_TEXT, Toast.LENGTH_LONG).show();
    }

    /*************************************
     * INTERSTITIAL METHODS AND RELATED
     *************************************/
    private InterstitialAd newInterstitialAd() {
        InterstitialAd interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
            }

            @Override
            public void onAdClosed() {
                // Proceed to the next level.
                goToNextLevel();
            }
        });
        return interstitialAd;
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and reload the ad.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
            goToNextLevel();
        }
    }

    private void loadInterstitial() {
        // Disable the next level button and load the ad.
        AdRequest adRequest = new AdRequest.Builder().setRequestAgent("android_studio:ad_template").build();
        mInterstitialAd.loadAd(adRequest);
    }

    private void goToNextLevel() {
        // Show the next level and reload the ad to prepare for the level after.
        mInterstitialAd = newInterstitialAd();
        loadInterstitial();
    }

    /*************************************
     * LOCATION METHODS AND RELATED
     *************************************/
    private void inicializarLocation() {
        //Para la localizacion
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);

        Location localizacion = getLastBestLocation();
        if (localizacion != null)
        {
            mLatitudTextView.setText("Latitud: " + localizacion.getLatitude());
            mLongitudTextView.setText("Longitud: " + localizacion.getLongitude());
        } else {
            Toast.makeText(this, "Error al obtener ubicación", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case LOCATION_PERMISO:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    inicializarLocation();
                }
                else
                {
                    Toast.makeText(this, "No se puede calcular nada si no concedes el permiso. Sal de la aplicación, vuelve a entrar, y concédelo", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    private Location getLastBestLocation() {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            }
            return null;
        }

        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS)
        {
            GPSLocationTime = locationGPS.getTime();
        }

        long NetLocationTime = 0;

        if (null != locationNet)
        {
            NetLocationTime = locationNet.getTime();
        }

        if ( 0 < GPSLocationTime - NetLocationTime )
        {
            return locationGPS;
        }
        else
        {
            return locationNet;
        }
    }
    /*---------- Listener class to get coordinates ------------- */
    private class MyLocationListener implements LocationListener
    {

        @Override
        public void onLocationChanged(Location loc)
        {
            //Toast.makeText(getBaseContext(), "Location changed: Lat: " + loc.getLatitude() + " Lng: " + loc.getLongitude(), Toast.LENGTH_SHORT).show();

            String longitude = "Longitud: " + loc.getLongitude();
            mLongitudTextView.setText(longitude);
            String latitude = "Latitud: " + loc.getLatitude();
            mLatitudTextView.setText(latitude);

            Location locationUni = new Location("UA");
            locationUni.setLatitude(38.3852667);
            locationUni.setLongitude(-0.5135819);

            Log.e("Distancia", ""+loc.distanceTo(locationUni));

            if (Math.abs(loc.distanceTo(locationUni))<100)
            {
                showInterstitial();
            }

        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }
}
