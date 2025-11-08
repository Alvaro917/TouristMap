package com.example.touristmap.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.example.touristmap.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapActivity extends AppCompatActivity {
    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;

    // Inicialización de la lista de lugares usando Arrays.asList()
    private final List<Lugar> todosLosLugares = Arrays.asList(
            new Lugar("Cerro San Cristóbal", -33.4246, -70.6346, R.drawable.ic_park, "Parque"),
            new Lugar("Parque Bicentenario", -33.40056, -70.60222, R.drawable.ic_park, "Parque"),
            new Lugar("Parque Forestal", -33.4349, -70.6416, R.drawable.ic_park, "Parque"),
            new Lugar("Museo de Bellas Artes", -33.4357, -70.6397, R.drawable.ic_museum, "Museo"),
            new Lugar("Museo Histórico Nacional", -33.4374, -70.6511, R.drawable.ic_museum, "Museo"),
            new Lugar("La Chascona (Casa Neruda)", -33.43116, -70.63448, R.drawable.ic_museum, "Cultura"),
            new Lugar("Centro Cultural GAM", -33.4395, -70.6398, R.drawable.ic_museum, "Cultura"),
            new Lugar("Costanera Center", -33.4170, -70.6068, R.drawable.ic_mall, "Tienda"),
            new Lugar("Patio Bellavista", -33.43417, -70.63506, R.drawable.ic_mall, "Tienda"),
            new Lugar("Estadio Nacional", -33.4631, -70.6106, R.drawable.ic_stadium, "Deporte"),
            new Lugar("Estadio Monumental David Arellano", -33.50648, -70.60598, R.drawable.ic_stadium, "Deporte"),
            new Lugar("Estadio Bicentenario de La Florida", -33.5378, -70.5737, R.drawable.ic_stadium, "Deporte"),
            new Lugar("Estadio Santa Laura", -33.4027, -70.6554, R.drawable.ic_stadium, "Deporte"),
            new Lugar("Estadio San Carlos de Apoquindo", -33.3909, -70.5004, R.drawable.ic_stadium, "Deporte"),
            new Lugar("Fantasilandia", -33.4603, -70.6628, R.drawable.ic_atraccions, "Entretención"),
            new Lugar("Templo Bahá'í", -33.47222, -70.50917, R.drawable.ic_tourism, "Turismo"),
            new Lugar("Palacio de La Moneda", -33.4430, -70.6533, R.drawable.ic_tourism, "Turismo")
    );
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    activarCapadeUbicacion();
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(14.0);


        List<String> categorias = Arrays.asList("Parque", "Tienda", "Museo", "Deporte", "Entretención", "Turismo");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_custom, categorias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = findViewById(R.id.filtromapa);
        spinner.setAdapter(adapter);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String categoriaSeleccionada = parent.getItemAtPosition(position).toString();
                filtrarMarcadores(categoriaSeleccionada);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });

        filtrarMarcadores("Todos");

        // Manejo de permisos (convertido de 'when' a 'if-else')
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            activarCapadeUbicacion();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }
    private void activarCapadeUbicacion() {
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        locationOverlay.enableMyLocation();
        mapView.getOverlays().add(locationOverlay);

        // runOnFirstFix con lambda en Java
        locationOverlay.runOnFirstFix(() -> {
            // runOnUiThread con lambda en Java
            runOnUiThread(() -> {
                mapView.getController().animateTo(locationOverlay.getMyLocation());
                mapView.getController().setZoom(18.0);
            });
        });
    }
    private void filtrarMarcadores(String categoria) {
        List<Marker> markersToRemove = new ArrayList<>();
        for (org.osmdroid.views.overlay.Overlay overlay : mapView.getOverlays()) {
            if (overlay instanceof Marker) {
                markersToRemove.add((Marker) overlay);
            }
        }
        mapView.getOverlays().removeAll(markersToRemove);

        List<Lugar> lugaresMostrar;
        if ("Todos".equals(categoria)) {
            lugaresMostrar = todosLosLugares;
        } else {
            lugaresMostrar = new ArrayList<>();
            for (Lugar lugar : todosLosLugares) {
                if (lugar.getCategoria().equals(categoria)) {
                    lugaresMostrar.add(lugar);
                }
            }
        }
        for (Lugar lugar : lugaresMostrar) {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(lugar.getLatitud(), lugar.getLongitud()));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(lugar.getNombre());
            marker.setIcon(ContextCompat.getDrawable(this, lugar.getIconoResId()));
            mapView.getOverlays().add(marker);
        }
        mapView.invalidate();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}