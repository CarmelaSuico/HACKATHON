package com.usc.lugarlang;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.api.IMapController;

public class MainActivity extends AppCompatActivity {

    private MapView map = null;
    private ImageButton btnHome, btnSearch, btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // OSMDroid Configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_main);

        // 1. Initialize Map
        initMap();

        // 2. Initialize Bottom Buttons (from the included layout)
        btnHome = findViewById(R.id.imghome);
        btnSearch = findViewById(R.id.imgsearch);
        btnSettings = findViewById(R.id.imgsettings);

        // 3. Set Click Listeners
        btnHome.setOnClickListener(v -> {
            // Return map to Cebu City Center
            map.getController().animateTo(new GeoPoint(10.3157, 123.8854));
            Toast.makeText(this, "Welcome Home!", Toast.LENGTH_SHORT).show();
        });

        btnSearch.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void initMap() {
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(15.0);
        GeoPoint startPoint = new GeoPoint(10.3157, 123.8854);
        mapController.setCenter(startPoint);
    }

    @Override
    public void onResume() { super.onResume(); map.onResume(); }

    @Override
    public void onPause() { super.onPause(); map.onPause(); }
}