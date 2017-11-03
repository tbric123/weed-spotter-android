package knightwing.ws.weedspotter.Views.Activities.SubmissionStages;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import knightwing.ws.weedspotter.Models.PlantIdentification.CurrentSubmission;
import knightwing.ws.weedspotter.Models.PlantIdentification.Utilities;
import knightwing.ws.weedspotter.R;
import knightwing.ws.weedspotter.Views.PlantIdentification.ReportActivity;

/**
 * Stage of submission where the location of the user is obtained, to guide on where the plant
 * being reported is located.
 */
public class CurrentLocationActivity extends FragmentActivity implements OnMapReadyCallback,
        LocationListener {

    // How often the location is updated
    final protected int GPS_UPDATE_INTERVAL = 5000;

    // Access to data as part of the current submission
    private CurrentSubmission submission = CurrentSubmission.getInstance();

    // Finding and displaying the location
    protected Location lastLocation;
    private ProgressBar locationFindingBar;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    Button btnStart;
    Button btnSkip;
    SharedPreferences locationSkipState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_c_loc);
        Toolbar toolbar = (Toolbar)(findViewById(R.id.location_tool_bar));
        toolbar.setTitle("Find your location");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        locationFindingBar = (ProgressBar)(findViewById(R.id.locationFindingBar));
        locationFindingBar.setVisibility(View.INVISIBLE);

        // Setup 'Next' button
        btnStart = (Button) findViewById(R.id.btn_start);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CurrentLocationActivity.this, ReportActivity.class));
            }
        });

        Utilities.setButtonEnabled(btnStart, false);

        locationSkipState = Utilities.getLocationSkipStateStorage(this);

        // Obtain 'Skip' button
        btnSkip = (Button) findViewById(R.id.skipButton);
        if (!btnSkip.isEnabled()) {
            Utilities.setLocationSkipState(locationSkipState, false, this);
        } else {
            Utilities.setLocationSkipState(locationSkipState, true, this);
        }

        // Check to see whether Location Finding is on, and begin looking
        if (!Utilities.hasLocationPermission(this)) {
            Utilities.requestLocationPermission(this);
            startLocationPolling();
        } else {
            System.out.println("Listening for location");
            promptForLocationSetting();
            startLocationPolling();
        }
    }

    /**
     * Skip the location finding process and just go to taking photos.
     * @param view screen where location finding process is being skipped
     * @require view != null
     * @ensure device doesn't override the manual location entry with the location it finds
     */
    public void skipLocation(View view) {
        startActivity(new Intent(CurrentLocationActivity.this, ReportActivity.class));
    }

    /**
     * Bring up a prompt to let the user know that they need to allow Location Finding in order
     * to find out where their submission will come from.
     */
    protected void promptForLocationSetting() {
        if (!Utilities.locationIsOn(this)) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle("Location is Required");
            dialogBuilder.setMessage("Please turn on your location setting to proceed.");
            dialogBuilder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(locationIntent, Utilities.LOCATION_SETTINGS_REQUEST);
                }
            });
            dialogBuilder.setIcon(R.drawable.alerticon);
            dialogBuilder.show();
        }
    }

    /**
     * Start checking where the user is located.
     */
    protected void startLocationPolling() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationFindingBar.setVisibility(View.VISIBLE);
        locationFindingBar.setIndeterminate(true);

        try {
            System.out.println("Searching...");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    GPS_UPDATE_INTERVAL, 0, this);
            lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch(SecurityException se) {
            System.out.println(se.toString());
        }

        // May already have the location
        if (lastLocation != null) {
            updateLocation(lastLocation);
        }
        System.out.println("Listening for location...");

    }

    /**
     * Specify the new location of the user.
     * @param location - the new location that was obtained.
     * @require location != null
     * @ensure location is set where the user actually is, even after this screen has passed.
     */
    protected void updateLocation(Location location) {
        this.lastLocation = location;
        TextView txtStatus = (TextView) findViewById(R.id.txt_status);
        txtStatus.setText(R.string.gps_found);
        locationFindingBar.setVisibility(View.GONE);
        Button btnStart = (Button) findViewById(R.id.btn_start);
        btnStart.setEnabled(true);

        submission.setLatitude(location.getLatitude());
        submission.setLongitude(location.getLongitude());
        mapFragment.getMapAsync(this);
        Utilities.setButtonEnabled(btnStart, true);
        Utilities.setButtonEnabled(btnSkip, false);
        Utilities.setLocationSkipState(locationSkipState, false, this);
    }

    /**
     * Bring up Location Settings Screen to switch Location Finding on.
     * @param requestCode - not used here.
     * @param permissions - all permissions for obtaining location
     * @param grantResults - whether or not the user is allowing the app to find their location.
     * @require requestCode != null && permissions != null && grantResults != null
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissions.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                promptForLocationSetting();
                startLocationPolling();
            }
        }
    }

    /**
     * When the user enables Location Finding, start looking for the location.
     * @param request - the Location Settings Screen
     * @param result - not used here
     * @param intent - not used here
     * @require request != null && result != null && intent != null
     */
    @Override
    protected void onActivityResult(int request, int result, Intent intent) {
        if (request == Utilities.LOCATION_SETTINGS_REQUEST) {
            startLocationPolling();
        }
    }

    /**
     * When location changes, update it.
     * @param location the new location.
     * @require location != null
     * @ensure the location is quickly updated
     */
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            if (lastLocation == null) {
                Toast.makeText(this, "Location acquired", Toast.LENGTH_SHORT).show();
            }
            updateLocation(location);
        }
    }

    /**
     * Not used.
     * @param provider
     * @param status
     * @param extras
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    /**
     * Placeholder method for when Location Finding is enabled.
     * @param provider - where permission to view the location is being changed
     * @require provider != null
     */
    @Override
    public void onProviderEnabled(String provider) {
        System.out.println(provider + " enabled...");
    }

    /**
     * Placeholder method for when Location Finding is disabled.
     * @param provider - where permission to view the location is being changed
     * @require provider != null
     */
    @Override
    public void onProviderDisabled(String provider) {
        System.out.println(provider + " disabled...");
    }

    /**
     * Redraw the user's location as a marker on the map.
     * @param googleMap - the map being drawn on
     * @require googleMap != null
     * @ensure the marker is pointing where the user actually is
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker to the current location, clearing previous markers
        mMap.clear();
        LatLng location = new LatLng(submission.getLatitude(), submission.getLongitude());
        mMap.addMarker(new MarkerOptions().position(location).title("You are here."));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 20));
    }
}
