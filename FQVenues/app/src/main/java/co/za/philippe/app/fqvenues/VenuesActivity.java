package co.za.philippe.app.fqvenues;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import co.za.philippe.app.fqvenues.UI.Adapter.PlacesAutoCompleteAdapter;
import co.za.philippe.app.fqvenues.UI.GridViewItem;
import co.za.philippe.fqc.Api;
import co.za.philippe.fqc.PlaceAPI;
import co.za.philippe.fqc.entities.Venue;

import co.za.philippe.app.fqvenues.R;


public class VenuesActivity extends AppCompatActivity implements LocationListener, GoogleMap.OnMarkerClickListener {



    private AutoCompleteTextView location;
    private Geocoder geocoder;
    private List<Address> addresses;
    private LocationManager locationManager;
    private ProgressDialog dialog;
    private GridView gridView;
    public Location mLastKnownLocation;
    private Context context = this;
    private Handler handler = new Handler();
    private GridAdapter adapter;

    private boolean viewMap=false;
    private MenuItem menuItem;
    private MapView mapView;
    private GoogleMap mMap;
    private Marker marker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

        getSupportActionBar().setTitle("Venues");

        locationManager =  (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        gridView = (GridView) findViewById(R.id.gridview);
        location = (AutoCompleteTextView) findViewById(R.id.location);
        location.setAdapter(new PlacesAutoCompleteAdapter(context, R.layout.autocomplete_list_item));
        dialog = new ProgressDialog(context, R.style.DialogTransparent);
        adapter =  new GridAdapter(context , new ArrayList<Venue>());
        mapView  = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Venue venue = adapter.getItem(i);
                System.out.println("Venue:"+ venue.getId());
                startActivity( new Intent(VenuesActivity.this, GalleryActivity.class).putExtra("venueId", venue.getId()) );

            }
        });

        location.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, int i, long l) {
                 new Thread(new Runnable() {
                     @Override
                     public void run() {
                         PlaceAPI placeApi  = new PlaceAPI();
                         final Location location =  placeApi.reverseGeocoding( ((TextView)view).getText().toString());
                         handler.post(new Runnable() {
                             @Override
                             public void run() {
                                 mLastKnownLocation = location;
                                 new DownloadVenues().execute();

                             }
                         });
                     }
                 }).start();
            }
        });


        if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }

        if (ContextCompat.checkSelfPermission(this,  Manifest.permission.ACCESS_FINE_LOCATION)  == PackageManager.PERMISSION_GRANTED)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.venues, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_toogle:

                if(viewMap){
                    item.setIcon(getDrawable(R.drawable.ic_apps_black_24dp));
                    viewMap=false;
                }else{
                    item.setIcon(getDrawable(R.drawable.ic_satellite_black_24dp));
                    viewMap=true;


                }
                new DownloadVenues().execute();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    public void onLocationChanged(Location location) {

        if(  mLastKnownLocation == null || ( mLastKnownLocation != null && mLastKnownLocation.distanceTo(location) > 300 ) ) {
            mLastKnownLocation = location;
            new DownloadVenues().execute();
        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private  void displayList(){
        if(!viewMap){
             mapView.setVisibility(View.GONE);
            gridView.setAdapter(adapter);
            gridView.setVisibility(View.VISIBLE);

        } else {
            mapView.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.GONE);
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    setUpMap(googleMap);
                }
            });
        }
    }

    private void setUpMap(GoogleMap map) {
        mMap = map;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), 13));
        ArrayList<Venue> venues = adapter.getVenues();


        for (int i = 0; i < venues.size(); i++) {
            Venue venue = venues.get(i);
            Marker m = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(venue.getLatitude(), venue.getLongitude()))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            m.setTag(venue.getId());
        }
        mMap.setOnMarkerClickListener(this);



    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String venueId = marker.getTag().toString();
        startActivity( new Intent(VenuesActivity.this, GalleryActivity.class).putExtra("vemueId",venueId));
        return false;
    }


    class GridAdapter extends ArrayAdapter<Venue> {

        LayoutInflater _inflater;
        ArrayList<Venue> results = null;
        ArrayList<Venue> arraylist;


        public GridAdapter(Context context, ArrayList<Venue> results) {
            super(context, R.layout.grid_row, results);
            _inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            this.results = results;
            this.arraylist = new ArrayList<Venue>();
            this.arraylist.addAll(this.results);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            Venue item = getItem(position);
            return buildView(view, item);
        }

        private View buildView(View view, final Venue item) {

            if (view == null) {
                view = _inflater.inflate(R.layout.grid_row, null);

            }

           final GridViewItem grid_img  = (GridViewItem)  view.findViewById(R.id.grid_img);

            new Thread( new Runnable(){

                @Override
                public void run() {
                    try {
                        URL url = new URL(item.getDisplayPhoto());
                        System.out.println(item.getDisplayPhoto());
                        final Bitmap mIcon = BitmapFactory.decodeStream(url.openConnection() .getInputStream());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                grid_img.setImageBitmap(mIcon);
                            }
                        });


                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            return view;
        }

        public ArrayList<Venue> getVenues(){
            return arraylist;
        }

    }






    class DownloadVenues extends AsyncTask<Void, Integer, ArrayList<Venue>> {
        protected void onPreExecute() {
            super.onPreExecute();

            dialog.setMessage("");
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.show();

            // dialog.sh
        }

        @Override
        protected ArrayList<Venue> doInBackground(Void... params) {
            Api api  = new Api();
            try {
                 return api.getVenues(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return new ArrayList<Venue>();
        }
        protected void onPostExecute(ArrayList<Venue> venues) {
            dialog.dismiss();
            adapter.clear();

            adapter.addAll(venues);
            displayList();


        }
    }
}
