package com.example.touristmap.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import com.example.touristmap.R;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapFragment extends Fragment {

    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;
    private List<Lugar> todosLosLugares = new ArrayList<>();

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) activarCapadeUbicacion();
            });
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()));
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        todosLosLugares = lugaresJSON();

        mapView = view.findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(14.0);

        List<String> categorias = new ArrayList<>(Arrays.asList("Parque", "Tienda", "Museo", "Deporte", "Entretención", "Turismo"));
        categorias.add(0, "Todos");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categorias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = view.findViewById(R.id.filtromapa);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filtrarMarcadores(parent.getItemAtPosition(position).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            activarCapadeUbicacion();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }
    private void activarCapadeUbicacion() {
        if (getContext() == null) return;
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getContext()), mapView);
        locationOverlay.enableMyLocation();
        mapView.getOverlays().add(locationOverlay);

        locationOverlay.runOnFirstFix(() -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    mapView.getController().animateTo(locationOverlay.getMyLocation());
                    mapView.getController().setZoom(16.0);
                });
            }
        });
    }
    private void filtrarMarcadores(String categoria) {
        List<Marker> markersToRemove = new ArrayList<>();
        for (org.osmdroid.views.overlay.Overlay overlay : mapView.getOverlays()) {
            if (overlay instanceof Marker) markersToRemove.add((Marker) overlay);
        }
        mapView.getOverlays().removeAll(markersToRemove);

        List<Lugar> lugaresMostrar = "Todos".equals(categoria) ? todosLosLugares : new ArrayList<>();
        if (!"Todos".equals(categoria)) {
            for (Lugar lugar : todosLosLugares) {
                if (lugar.getCategoria().equals(categoria)) lugaresMostrar.add(lugar);
            }
        }
        for (Lugar lugar : lugaresMostrar) {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(lugar.getLatitud(), lugar.getLongitud()));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(lugar.getNombre());
            marker.setIcon(ContextCompat.getDrawable(requireContext(), lugar.getIconoResId()));
            mapView.getOverlays().add(marker);
        }
        mapView.invalidate();
    }
    private List<Lugar> lugaresJSON() {
        List<Lugar> lugares = new ArrayList<>();
        try {
            InputStream is = requireContext().getAssets().open("lugares.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                lugares.add(new Lugar(
                        obj.getString("nombre"),
                        obj.getDouble("latitud"),
                        obj.getDouble("longitud"),
                        getIconoId(obj.getString("icono")),
                        obj.getString("categoria")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lugares;
    }
    private int getIconoId(String nombre) {
        switch (nombre) {
            case "ic_park": return R.drawable.ic_park;
            case "ic_museum": return R.drawable.ic_museum;
            case "ic_mall": return R.drawable.ic_mall;
            case "ic_stadium": return R.drawable.ic_stadium;
            case "ic_atraccions": return R.drawable.ic_atraccions;
            case "ic_tourism": return R.drawable.ic_tourism;
            default: return R.drawable.ic_park;
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }
}