package com.example.roadsideassistance;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.Vector;

public class RoadsideServiceSelect extends FragmentActivity implements OnMapReadyCallback {
    RoadsideAssistant roadsideAssistant;

    public AppDatabase database;
    private GoogleMap map;
    private Location currLocation = null;
    private double radius = 10;
    private int selectedServiceIndex = 0;
    private  Vector<Service> servicesInRadius;
    private Vector<Double> serviceDistances;
    private byte filter = 0b0;
    private SupportMapFragment mapGlobalFragment;

    private Spinner radiusSpinner;
    private ArrayAdapter<CharSequence> radiAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roadside_service_select);

        //get Roadside assistant
        roadsideAssistant = getIntent().getParcelableExtra("Roadside");

        //delete database before use
        //this.deleteDatabase("appdatabase");
        database = AppDatabase.getDatabase(getApplicationContext());

        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.serviceSelectMap);
        mapGlobalFragment = mapFragment;

        radiusSpinner = findViewById(R.id.radiusSpinner);
        radiAdapter = ArrayAdapter.createFromResource(this, R.array.radii, android.R.layout.simple_spinner_item);
        radiusSpinner.setAdapter(radiAdapter);

        radiusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                radius = Integer.parseInt(radiusSpinner.getItemAtPosition(position).toString());
                mapFragment.getMapAsync(getOnMapReadyCallback());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.clear();
        map.getUiSettings().setRotateGesturesEnabled(false);

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<Location> locationResult = LocationServices.getFusedLocationProviderClient(this).getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        //Toast.makeText(getApplicationContext(), "Task good", Toast.LENGTH_LONG).show();
                        currLocation = task.getResult();
                        if (currLocation != null) {
                            LatLng currentPosition = new LatLng(currLocation.getLatitude(), currLocation.getLongitude());
                            LatLng diffLatLng = LatitudeLongitude.getDifferences(currentPosition.latitude, currentPosition.longitude, radius);

                            //move the camera to current location and zoom to city level
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 10 - (float)radiusSpinner.getSelectedItemPosition()/1.5f));

                            //Create circle to show radius of search
                            map.addCircle(new CircleOptions()
                                    .center(currentPosition)
                                    .radius(radius*1e3)
                                    .strokeColor(getResources().getColor(R.color.mapRadius))
                                    .strokeWidth(4)
                                    .fillColor(getResources().getColor(R.color.mapFill)));

                            List<Service> servicesInArea = database.serviceDao().getNewServiceRequests(
                                    currentPosition.latitude - diffLatLng.latitude,
                                    currentPosition.latitude + diffLatLng.latitude,
                                    currentPosition.longitude - diffLatLng.longitude,
                                    currentPosition.longitude + diffLatLng.longitude);

                            servicesInRadius = new Vector<>();
                            serviceDistances = new Vector<>();

                            //Get all services in radius from all services in the square area
                            if(servicesInArea.size() > 0) {
                                for (int i = 0; i < servicesInArea.size(); i++) {
                                    if ((LatitudeLongitude.distance(currentPosition, new LatLng(servicesInArea.get(i).latitude, servicesInArea.get(i).longitude)) <= radius)
                                        && !database.serviceDao().madeOffer(roadsideAssistant.username, servicesInArea.get(i).customer_username, servicesInArea.get(i).car_plateNum, servicesInArea.get(i).time)
                                        && ((filter == 0) || (servicesInArea.get(i).filter == filter)))
                                        servicesInRadius.add(servicesInArea.get(i));
                                }
                            }

                            final LinearLayout servicesListLayout = findViewById(R.id.roadsideServicesSelectList);
                            servicesListLayout.removeViews(0, servicesListLayout.getChildCount());
                            if(servicesInRadius.size() > 0) {
                                for (int i = 0; i < servicesInRadius.size(); i++) {
                                    final int currIndex = i;

                                    //Add the service to the map
                                    map.addMarker(new MarkerOptions().position(new LatLng(servicesInRadius.get(i).latitude, servicesInRadius.get(i).longitude)));
                                    //Add to distance array
                                    serviceDistances.add(LatitudeLongitude.distance(currentPosition, new LatLng(servicesInRadius.get(i).latitude, servicesInRadius.get(i).longitude)));

                                    final LinearLayout servicesLayout = new LinearLayout(getContext());
                                    servicesLayout.setOrientation(LinearLayout.HORIZONTAL);
                                    servicesLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                                    TextView custUsernameText = new TextView(getContext());
                                    custUsernameText.setPadding(5,5,5,5);
                                    custUsernameText.setWidth(findViewById(R.id.username).getWidth());
                                    custUsernameText.setText(servicesInRadius.get(i).customer_username);
                                    custUsernameText.setBackground(getResources().getDrawable(R.drawable.border_sharp));
                                    servicesLayout.addView(custUsernameText);

                                    TextView plateNumText = new TextView(getContext());
                                    plateNumText.setPadding(5,5,5,5);
                                    plateNumText.setWidth(findViewById(R.id.payType).getWidth());
                                    plateNumText.setText(servicesInRadius.get(i).car_plateNum);
                                    plateNumText.setBackground(getResources().getDrawable(R.drawable.border_sharp));
                                    servicesLayout.addView(plateNumText);

                                    TextView distText = new TextView(getContext());
                                    distText.setPadding(5,5,5,5);
                                    distText.setWidth(findViewById(R.id.distance).getWidth());
                                    distText.setText("" + serviceDistances.get(i).intValue() + "km");
                                    distText.setBackground(getResources().getDrawable(R.drawable.border_sharp));
                                    servicesLayout.addView(distText);

                                    servicesLayout.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            selectedServiceIndex = currIndex;
                                            for (int j = 0; j < servicesListLayout.getChildCount(); j++) {
                                                servicesListLayout.getChildAt(j).setBackgroundColor(getResources().getColor(R.color.unselectedListItem));
                                            }
                                            servicesLayout.setBackgroundColor(getResources().getColor(R.color.selectedListItem));
                                        }
                                    });

                                    servicesListLayout.addView(servicesLayout);

                                    /*
                                    //Create text box to add to list of services
                                    final TextView addedTextView = new TextView(getContext());
                                    addedTextView.setText(servicesInRadius.get(i).toString() +
                                            " Dist = " + serviceDistances.get(i).intValue() + "km");

                                    //Set function when service is selected
                                    addedTextView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            selectedServiceIndex = currIndex;
                                            for (int j = 0; j < servicesListLayout.getChildCount(); j++) {
                                                servicesListLayout.getChildAt(j).setBackgroundColor(getResources().getColor(R.color.unselectedListItem));
                                            }
                                            addedTextView.setBackgroundColor(getResources().getColor(R.color.selectedListItem));
                                        }
                                    });

                                    //Set constraints and layout
                                    addedTextView.setPadding(0, 10, 0, 10);
                                    addedTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

                                    //Add it to the list
                                    servicesListLayout.addView(addedTextView);
                                    */
                                }
                            }
                            //Show no services when there aren't any in the area
                            else {
                                final TextView noServiceTextView = new TextView(getContext());
                                noServiceTextView.setText("No Services in Area, Maybe Change your filter options");
                                noServiceTextView.setPadding(0, 10, 0, 10);
                                noServiceTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                                servicesListLayout.addView(noServiceTextView);

                                //Hide select button so it can't be clicked
                                findViewById(R.id.roadSideSelectServiceButton).setVisibility(View.GONE);
                            }
                        }
                    }
                }
            });
        }
        else
            Toast.makeText(this, "Not Permitted to use GPS", Toast.LENGTH_LONG).show();
    }

    public void onClick(View view) {
        //Toast.makeText(this, "Selected service = " + servicesInRadius.get(selectedServiceIndex).toString(), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, RoadsideSelectServiceCost.class);
        intent.putExtra("Service", servicesInRadius.get(selectedServiceIndex));
        intent.putExtra("RoadsideAssistant", roadsideAssistant);
        intent.putExtra("Distance", serviceDistances.get(selectedServiceIndex));
        startActivityForResult(intent,1 );
    }

    public void toSetFilter(View view) {
        Intent intent = new Intent(this, RoadsideSetFilter.class);
        intent.putExtra("Filter", filter);
        startActivityForResult(intent, 2);
    }

    //Used to get context while in callback
    public Context getContext() {
        return (Context)this;
    }

    public OnMapReadyCallback getOnMapReadyCallback() {return this;}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK && requestCode == 1) {
            roadsideAssistant = data.getParcelableExtra("Roadside");
            finish();
        }
        if(resultCode == RESULT_OK && requestCode == 2) {
            filter = data.getByteExtra("Filter", (byte)0);
            mapGlobalFragment.getMapAsync(this);
        }
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        //if(roadsideAssistant == null)
            //Toast.makeText(this, "Roadside NULL", Toast.LENGTH_LONG).show();
        data.putExtra("Roadside", roadsideAssistant);
        setResult(RESULT_OK, data);
        super.finish();
    }
}
