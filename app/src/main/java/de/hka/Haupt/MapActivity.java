package de.hka.Haupt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


import Netzwerk.EfaApiClient;
import Objekte.EfaCoordResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class MapActivity extends AppCompatActivity {




    private MapView mapView;

    String string3;

    String string5;

   List Liste1;

    int wert;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);









        Button btn_score = this.findViewById(R.id.Score);

        TextView ausgabe= this.findViewById(R.id.ausgabe_score);

        btn_score.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                ausgabe.setText(string3);
            }
        });

        XYTileSource mapServer = new XYTileSource(
                "MapName",
                8,
                20,
                256,
                ".png",
                new String[]{"https://tileserver.svprod01.app/styles/default/"}
        );

       String authorizationString = this.getMapServerAuthorizationString(
               "ws2223@hka",
               "LeevwBfDi#2027"

       );

        Configuration
                .getInstance()
                .getAdditionalHttpRequestProperties()
                .put("Authorization", authorizationString);

        this.mapView = this.findViewById(R.id.mapView);
        this.mapView.setTileSource(mapServer);

        GeoPoint startPoint = new GeoPoint(49.0000, 8.4690);

        IMapController mapController = this.mapView.getController();
        mapController.setCenter(startPoint);
        mapController.setZoom(15.0);


        this.mapView.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                loadClosestStops(mapView.getMapCenter().getLatitude(), mapView.getMapCenter().getLongitude());
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                loadClosestStops(mapView.getMapCenter().getLatitude(), mapView.getMapCenter().getLongitude());
                return false;
            }
        });


    }









    @Override
    protected void onResume() {
        super.onResume();

        String[] permissions = new String[]{

                Manifest.permission.ACCESS_FINE_LOCATION

        };


        Permissions.check(this, permissions, null, null, new PermissionHandler() {
            @Override
            public void onGranted() {

                initlocationListener();

                Log.d("MapActivity", "onGranted");

            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                super.onDenied(context, deniedPermissions);

                Log.d("MapActivity", "onDenied");
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void initlocationListener (){

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double latitude = location.getLatitude();  // get aktuelle breite
                double longitude = location.getLongitude();   // get aktuelle Länge

                GeoPoint startPoint = new GeoPoint(latitude, longitude); //neuer geopoint mit  Breite und Länge

                IMapController mapController = mapView.getController();
                mapController.setCenter(startPoint);





            }
        };

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, // daten ziehen
                2000,                           // alle 2000ms
                10,                             //min 10m bewegung
                locationListener
        );

    }


    private String getMapServerAuthorizationString(String username, String password)
    {
        String authorizationString = String.format("%s:%s", username, password);
        return "Basic " + Base64.encodeToString(authorizationString.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
    }

    private void loadClosestStops(double latitude, double longitude) {
        retrofit2.Call<EfaCoordResponse> efaCall = EfaApiClient
                .getInstance()
                .getClient()
                .loadStopsWithinRadius(
                        EfaApiClient
                                .getInstance()
                                .createCoordinateString(
                                        latitude,
                                        longitude
                                ),
                        500
                );

        efaCall.enqueue(new Callback<EfaCoordResponse>() {

            @Override
            public void onResponse(retrofit2.Call<EfaCoordResponse> call, Response<EfaCoordResponse> response) {

               

                Log.d("MapActivity", String.format("Response %d Locations", response.body().getLocations().size()));
               wert= (response.body().getLocations().size());
                string3 = ("Score: "+ wert);
                 Liste1=((response.body().getLocations()));


                for (int i = 0; i < Liste1.size(); i++) {
                    System.out.println(Liste1.get(i));


                }

                for (int i = 0; i < response.body().getLocations().size(); i++) {
                    System.out.println("ausgabe "+response.body().getLocations().get(i));




                }



                System.out.println(Liste1);









            }

            @Override
            public void onFailure(Call<EfaCoordResponse> call, Throwable t) {
                Log.d("MapActivity", "Failure" );

            }

        });

    }
}