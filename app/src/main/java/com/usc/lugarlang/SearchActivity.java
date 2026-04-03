package com.usc.lugarlang;

import android.graphics.Color;
import android.location.Address;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    MapView map;
    EditText editOrigin, editDestination;
    ImageButton btnHome;
    Button btnSearch;
    FloatingActionButton btnZoomIn, btnZoomOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Finding the ids
        btnZoomIn = findViewById(R.id.btn_zoom_in);
        btnZoomOut = findViewById(R.id.btn_zoom_out);
        btnHome = findViewById(R.id.imghome);
        map = findViewById(R.id.search_map);

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_search);

        editOrigin = findViewById(R.id.edit_origin);
        editDestination = findViewById(R.id.edit_destination);

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);
        map.getController().setCenter(new GeoPoint(10.3157, 123.8854));

        // NAVIGATION LOGIC
        btnHome.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(SearchActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // THE NEW SEARCH BUTTON LOGIC
        btnSearch = findViewById(R.id.btnsearch); // Linked to your bottom nav search icon
        btnSearch.setOnClickListener(v -> validateAndStartRouting());

        // Keyboard "Enter" trigger
        editDestination.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                validateAndStartRouting();
                return true;
            }
            return false;
        });

        btnZoomIn.setOnClickListener(v -> {
            map.getController().zoomIn();
        });

        btnZoomOut.setOnClickListener(v -> {
            map.getController().zoomOut();
        });
    }

    private void validateAndStartRouting() {
        String origin = editOrigin.getText().toString().trim();
        String destination = editDestination.getText().toString().trim();

        if (!origin.isEmpty() && !destination.isEmpty()) {
            startNavigation(origin, destination);
        } else {
            Toast.makeText(this, "Please fill both fields", Toast.LENGTH_SHORT).show();
        }
    }

    private void startNavigation(String originName, String destName) {
        new Thread(() -> {
            try {
                // Using UserAgent is required by OSM servers
                GeocoderNominatim geocoder = new GeocoderNominatim("LugarLang_App");

                List<Address> startAddresses = geocoder.getFromLocationName(originName + ", Cebu", 1);
                List<Address> endAddresses = geocoder.getFromLocationName(destName + ", Cebu", 1);

                if (startAddresses != null && !startAddresses.isEmpty() && endAddresses != null && !endAddresses.isEmpty()) {
                    GeoPoint startPoint = new GeoPoint(startAddresses.get(0).getLatitude(), startAddresses.get(0).getLongitude());
                    GeoPoint endPoint = new GeoPoint(endAddresses.get(0).getLatitude(), endAddresses.get(0).getLongitude());

                    RoadManager roadManager = new OSRMRoadManager(this, "LugarLang_App");
                    ArrayList<GeoPoint> waypoints = new ArrayList<>();
                    waypoints.add(startPoint);
                    waypoints.add(endPoint);

                    Road road = roadManager.getRoad(waypoints);
                    Polyline roadOverlay = RoadManager.buildRoadOverlay(road);

                    roadOverlay.setColor(Color.parseColor("#B0006D"));
                    roadOverlay.setWidth(30.0f);

                    runOnUiThread(() -> {
                        map.getOverlays().clear();
                        map.getOverlays().add(roadOverlay);
                        // Animate to center the route
                        map.getController().animateTo(startPoint);
                        map.invalidate();
                        Toast.makeText(this, "Route Found!", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Location not found in Cebu", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error connecting to routing server", Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        }).start();
    }

    @Override public void onResume() { super.onResume(); map.onResume(); }
    @Override public void onPause() { super.onPause(); map.onPause(); }
}