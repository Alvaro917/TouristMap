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
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MapActivity extends AppCompatActivity {
    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;
    private List<Lugar> todosLosLugares = new ArrayList<>();
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
        todosLosLugares = lugaresJSON();

        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(14.0);

        List<String> categorias = new ArrayList<>(Arrays.asList("Parque", "Tienda", "Museo", "Deporte", "Entretención", "Turismo"));
        categorias.add(0, "Todos");

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
    private List<Lugar> lugaresJSON(){
        List<Lugar> lugares = new ArrayList<>();
        try {
            String jsonString = loadJSONFromAsset();
            if (jsonString == null){
                return lugares;
            }
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject lugarJson = jsonArray.getJSONObject(i);

                String nombre = lugarJson.getString("nombre");
                double latitud = lugarJson.getDouble("latitud");
                double longitud = lugarJson.getDouble("longitud");
                String iconoNombre = lugarJson.getString("icono");
                String categoria = lugarJson.getString("categoria");

                int iconoId = getIconoId(iconoNombre);
                lugares.add(new Lugar(nombre,latitud,longitud,iconoId,categoria));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return lugares;
    }
    private String loadJSONFromAsset() {
        String json;
        try {
            InputStream is = getAssets().open("lugares.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
    private int getIconoId(String iconoNombre){
        switch (iconoNombre){
            case "ic_park":
                return R.drawable.ic_park;
            case "ic_museum":
                return R.drawable.ic_museum;
            case "ic_mall":
                return R.drawable.ic_mall;
            case "ic_stadium":
                return R.drawable.ic_stadium;
            case "ic_atraccions":
                return R.drawable.ic_atraccions;
            case "ic_tourism":
                return R.drawable.ic_tourism;
            default:
                // Un ícono por defecto si no se encuentra
                return R.drawable.ic_park;
        }
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