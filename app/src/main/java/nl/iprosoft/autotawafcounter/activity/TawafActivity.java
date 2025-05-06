package nl.iprosoft.autotawafcounter.activity;

import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;

import android.Manifest;
import android.animation.Animator;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.ImageHolder;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.animation.CameraAnimationsPlugin;
import com.mapbox.maps.plugin.animation.CameraAnimationsUtils;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationType;
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;

import java.util.ArrayList;
import java.util.List;

import nl.iprosoft.autotawafcounter.R;

public class TawafActivity extends AppCompatActivity {

    // Constants
    private final double KABA_LONGITUDE = 39.8262;
    private final double KABA_LATITUDE = 21.4225;
    private final double ZOOM_LEVEL = 17.50;
    private static final int TAWAF_RADIUS_THRESHOLD = 200; // meters
    private static final int TOTAL_TAWAF = 7;
    private final int[] LINE_COLORS = {
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
            Color.MAGENTA, Color.CYAN, Color.WHITE
    };

    // Views
    private MapView mapView;
    private TextView tawafCounterText;
    private FloatingActionButton floatingActionButton;
    private FloatingActionButton zoomIn;
    private FloatingActionButton zoomOut;

    // Tracking variables
    private int tawafCount = 0;
    private boolean canCountTawaf = true;
    private Point startPoint;
    private final List<Point> currentPath = new ArrayList<>();
    private PolylineAnnotationManager polylineAnnotationManager;
    private Vibrator vibrator;

    // Mapbox components
    private final ActivityResultLauncher<String> activityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
                if (result) {
                    startLocationTracking();
                }
            });

    // Location listeners
    private final OnIndicatorBearingChangedListener onIndicatorBearingChangedListener = v ->
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().bearing(v).build());

    private final OnIndicatorPositionChangedListener onIndicatorPositionChangedListener = point -> {
        updateCameraPosition(point);
        checkTawafCompletion(point);
        updatePathDrawing(point);
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
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tawaf);

        initializeViews();
        setupPermissions();
        setupMap();

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        startPoint = Point.fromLngLat(KABA_LONGITUDE, KABA_LATITUDE);
        setKabaLocation();
    }

    private void initializeViews() {
        mapView = findViewById(R.id.mapView);
        tawafCounterText = findViewById(R.id.tawafCounter);
        floatingActionButton = findViewById(R.id.focusLocation);
        zoomIn = findViewById(R.id.zoomIn);
        zoomOut = findViewById(R.id.zoomOut);
    }

    private void setupPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void setupMap() {
        mapView.getMapboxMap().loadStyle(Style.SATELLITE, style -> {
            initializeLocationComponent();
            setupAnnotationPlugin();
            setupCamera();
            setupClickListeners();
        });
    }

    private void initializeLocationComponent() {
        LocationComponentPlugin locationComponent = getLocationComponent(mapView);
        locationComponent.setEnabled(true);
        LocationPuck2D puck = new LocationPuck2D();
        puck.setBearingImage(ImageHolder.from(R.drawable.baseline_location_on_24));
        locationComponent.setLocationPuck(puck);
    }

    private void setupAnnotationPlugin() {
        AnnotationPlugin annotationPlugin = mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);
        if (annotationPlugin != null) {
            polylineAnnotationManager = (PolylineAnnotationManager)
                    annotationPlugin.createAnnotationManager(
                            AnnotationType.PolylineAnnotation,
                            new AnnotationConfig()
                    );
        } else {
            Log.e("Mapbox", "AnnotationPlugin not available.");
        }
    }
    private void startLocationTracking() {
        LocationComponentPlugin locationComponent = getLocationComponent(mapView);
        locationComponent.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
        locationComponent.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
        getGestures(mapView).addOnMoveListener(onMoveListener);
    }

    private void updateCameraPosition(Point currentPoint) {
        mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                .center(startPoint)
                .zoom(mapView.getMapboxMap().getCameraState().getZoom())
                .build());
        getGestures(mapView).setFocalPoint(mapView.getMapboxMap().pixelForCoordinate(startPoint));
    }

    private void checkTawafCompletion(Point currentPoint) {
        float[] results = new float[1];
        android.location.Location.distanceBetween(
                startPoint.latitude(), startPoint.longitude(),
                currentPoint.latitude(), currentPoint.longitude(),
                results
        );

        if (results[0] <= TAWAF_RADIUS_THRESHOLD) {
            if (canCountTawaf && tawafCount < TOTAL_TAWAF) {
                handleTawafCompletion();
            }
        } else {
            canCountTawaf = true;
        }
    }

    private void handleTawafCompletion() {
        tawafCount++;
        canCountTawaf = false;

        // Update UI
        runOnUiThread(() -> {
            tawafCounterText.setText(String.format("%d/7 Tawaf", tawafCount));
            showCompletionDialog();
            vibrateDevice();
        });

        if (tawafCount == TOTAL_TAWAF) {
            navigateToCompletionScreen();
        }
    }

    private void updatePathDrawing(Point newPoint) {
        currentPath.add(newPoint);

        PolylineAnnotationOptions options = new PolylineAnnotationOptions()
                .withPoints(currentPath)
                .withLineColor(LINE_COLORS[tawafCount])
                .withLineWidth(5.0);
        polylineAnnotationManager.create(options);
    }

    private void showCompletionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Mashallah!")
                .setMessage(tawafCount + "/7 Tawaf completed. Continue to next circuit.")
                .setPositiveButton("Continue", null)
                .show();
    }

    private void vibrateDevice() {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(500);
        }
    }

    private void navigateToCompletionScreen() {
        // startActivity(new Intent(this, CompletionActivity.class));
        finish();
    }

    private void setupClickListeners() {
        floatingActionButton.setOnClickListener(v -> {
            getLocationComponent(mapView).addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
            getLocationComponent(mapView).addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
            getGestures(mapView).addOnMoveListener(onMoveListener);
            updateCameraPosition(startPoint);
            setKabaLocation();
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

    private void setupCamera() {
        // Create camera position
        CameraOptions cameraOptions = new CameraOptions.Builder()
                .center(Point.fromLngLat(KABA_LONGITUDE, KABA_LATITUDE))
                .zoom(ZOOM_LEVEL)
                .build();

        final CameraAnimationsPlugin camera = CameraAnimationsUtils.getCamera(mapView);

        // Implement Animator.AnimatorListener
        Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.d("CameraAnimation", "Animation started");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d("CameraAnimation", "Animation ended");
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.d("CameraAnimation", "Animation canceled");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // Optional if needed
                Log.d("onAnimationRepeat", "onAnimationRepeat hit");
            }
        };

        // Use flyTo with listener
        camera.flyTo(cameraOptions, new MapAnimationOptions.Builder().duration(4000).build(), animatorListener);
    }

    private void setKabaLocation(){
        mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                .center(startPoint)
                .zoom(ZOOM_LEVEL)
                .build());
    }

}