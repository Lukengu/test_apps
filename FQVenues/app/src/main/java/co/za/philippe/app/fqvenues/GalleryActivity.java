package co.za.philippe.app.fqvenues;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import co.za.philippe.app.fqvenues.UI.GridViewItem;
import co.za.philippe.fqc.Api;
import co.za.philippe.fqc.entities.Photo;
import co.za.philippe.fqc.entities.Venue;
import co.za.philippe.app.fqvenues.R;

public class GalleryActivity extends AppCompatActivity {

    Handler handler = new Handler();
    private ProgressDialog dialog;
    private GridView gridView;
    private  GridAdapter adapter;
    private SharedPreferences spref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        adapter = new GridAdapter(this, new ArrayList<Photo>());

        dialog = new ProgressDialog(this, R.style.DialogTransparent);
        gridView = (GridView) findViewById(R.id.gridview);
        getSupportActionBar().setTitle("Gallery");

        spref =  PreferenceManager.getDefaultSharedPreferences(this);
        String venueId ="";

        Bundle b = getIntent().getExtras();
        if( b != null && b.containsKey("venueId")) {
            venueId = b.getString("venueId");
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putString("venueId", venueId).commit();
        } else {
            venueId = spref.getString("venueId","");
        }



        new DownloadPhotos().execute(new String[]{venueId});

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Photo photo = adapter.getItem(i);
                Bundle b = new Bundle();
                b.putSerializable("photo", photo);
                startActivity(new Intent(GalleryActivity.this, PhotoActivity.class).putExtras(b));
            }
        });

    }


    class GridAdapter extends ArrayAdapter<Photo> {
        LayoutInflater _inflater;
        ArrayList<Photo> results = null;
        ArrayList<Photo> arraylist;



        public GridAdapter(Context context, ArrayList<Photo> results) {

            super(context, R.layout.grid_row, results);

            _inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            this.results = results;
            this.arraylist = new ArrayList<Photo>();
            this.arraylist.addAll(this.results);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            Photo item = getItem(position);

            return buildView(view, item);
        }

        private View buildView(View view, final Photo item) {



            if (view == null) {
                view = _inflater.inflate(R.layout.grid_row, null);

            }

            final GridViewItem grid_img  = (GridViewItem)  view.findViewById(R.id.grid_img);

            new Thread( new Runnable(){

                @Override
                public void run() {
                    try {
                        URL url = new URL(item.getUrl());
                        System.out.println(item.getUrl());
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

    }
    class DownloadPhotos extends AsyncTask<String, Integer, ArrayList<Photo>> {
        protected void onPreExecute() {
            super.onPreExecute();

            dialog.setMessage("");
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.show();
            // dialog.sh
        }

        @Override
        protected ArrayList<Photo> doInBackground(String... params) {
            Api api  = new Api();
            try {
                return api.getPhotos(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return new ArrayList<Photo>();
        }
        protected void onPostExecute(ArrayList<Photo> photos) {
            dialog.dismiss();
            adapter.clear();
            adapter.addAll(photos);
            gridView.setAdapter(adapter);

        }
    }
}
