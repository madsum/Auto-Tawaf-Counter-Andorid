package nl.iprosoft.autotawafcounter.activity;

import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.ImageHolder;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;

import nl.iprosoft.autotawafcounter.R;

public class TawafActivity extends AppCompatActivity {


    private final double KABA_LONGITUDE = 39.8262;
    private final double KABA_LATITUE = 21.4225;

    private double zoomLevel = 17.50;
    private MapView mapView;
    private FloatingActionButton floatingActionButton;
    private FloatingActionButton zoomIn;
    private FloatingActionButton zoomOut;
    private boolean isUserRequestedFocus = true;

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            if (result) {
                Toast.makeText(TawafActivity.this, "Permission granted!", Toast.LENGTH_SHORT).show();
            }
        }
    });

    private final OnIndicatorBearingChangedListener onIndicatorBearingChangedListener = new OnIndicatorBearingChangedListener() {
        @Override
        public void onIndicatorBearingChanged(double v) {
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().bearing(v).build());
        }
    };

    private final OnIndicatorPositionChangedListener onIndicatorPositionChangedListener = new OnIndicatorPositionChangedListener() {
        @Override
        public void onIndicatorPositionChanged(@NonNull Point point) {
            // set current location Kaba.
            double currentZoom = mapView.getMapboxMap().getCameraState().getZoom();
            Point kabaPoint = Point.fromLngLat(KABA_LONGITUDE, KABA_LATITUE);
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                    .center(kabaPoint)
                    .zoom(currentZoom)
                    .build());
            getGestures(mapView).setFocalPoint(mapView.getMapboxMap().pixelForCoordinate(kabaPoint));
        }
    };

    private final OnMoveListener onMoveListener = new OnMoveListener() {
        @Override
        public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {
            getLocationComponent(mapView).removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
            getLocationComponent(mapView).removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
            getGestures(mapView).removeOnMoveListener(onMoveListener);
            floatingActionButton.show();
        }

        @Override
        public boolean onMove(@NonNull MoveGestureDetector moveGestureDetector) {
            return false;
        }

        @Override
        public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {
            // No-op
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tawaf);

        mapView = findViewById(R.id.mapView);
        floatingActionButton = findViewById(R.id.focusLocation);
        zoomIn = findViewById(R.id.zoomIn);
        zoomOut = findViewById(R.id.zoomOut);

        if (ActivityCompat.checkSelfPermission(TawafActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        floatingActionButton.show();

        mapView.getMapboxMap().loadStyle(
                Style.SATELLITE, style -> {
                    setKabaLocation();
/*
                    Point targetPoint = Point.fromLngLat(KABA_LONGITUDE, KABA_LATITUE);
                    mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                            .center(targetPoint)
                            .zoom(zoomLevel)
                            .build());
*/

                    LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
                    locationComponentPlugin.setEnabled(true);

                    LocationPuck2D locationPuck2D = new LocationPuck2D();
                    locationPuck2D.setBearingImage(ImageHolder.from(R.drawable.baseline_location_on_24));
                    locationComponentPlugin.setLocationPuck(locationPuck2D);

                    locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
                    locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
                    getGestures(mapView).addOnMoveListener(onMoveListener);

                    floatingActionButton.setOnClickListener(view -> {
                        locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
                        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
                        getGestures(mapView).addOnMoveListener(onMoveListener);
                        setKabaLocation();
                    });
                });

        zoomIn.setOnClickListener(v -> {
            double currentZoom = mapView.getMapboxMap().getCameraState().getZoom();
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                    .zoom(currentZoom + 1.0)
                    .build());
        });

        zoomOut.setOnClickListener(v -> {
            double currentZoom = mapView.getMapboxMap().getCameraState().getZoom();
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                    .zoom(currentZoom - 1.0)
                    .build());
        });
    }
    private void setKabaLocation(){
        Point targetPoint = Point.fromLngLat(KABA_LONGITUDE, KABA_LATITUE);
        mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                .center(targetPoint)
                .zoom(zoomLevel)
                .build());
    }
}