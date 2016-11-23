package co.za.philippe.app.fqvenues;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import co.za.philippe.fqc.entities.Photo;

public class PhotoActivity extends AppCompatActivity {
    TextView username,date;
    ImageView image;
    Handler  handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_photo);
        Bundle b = getIntent().getExtras();
        getSupportActionBar().setTitle("Photo");
       SharedPreferences spref =  PreferenceManager.getDefaultSharedPreferences(this);
        username = (TextView) findViewById(R.id.name);
        date = (TextView) findViewById(R.id.date);
        image = (ImageView ) findViewById(R.id.image);

        if(b != null  && b.containsKey("photo")) {
            final Photo photo = (Photo) b.getSerializable("photo");
            try {
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putString ("photo", photo.toJSON() ).commit();

                username.setText("By "+photo.getFirstName()+" "+photo.getSurname());
                date.setText("On "+photo.getCreatedAt());
                new Thread( new Runnable(){

                    @Override
                    public void run() {
                        try {
                            URL url = new URL(photo.getUrl());
                            final Bitmap mIcon = BitmapFactory.decodeStream(url.openConnection() .getInputStream());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    image.setImageBitmap(mIcon);
                                }
                            });


                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            try {
                final JSONObject photo   = new JSONObject(spref.getString("photo",""));
                username.setText("By "+photo.optString("firstName")+" "+photo.optString("surname"));
                date.setText("On "+photo.optString("createdAt"));
                new Thread( new Runnable(){

                    @Override
                    public void run() {
                        try {
                            URL url = new URL(photo.getString("url"));
                            final Bitmap mIcon = BitmapFactory.decodeStream(url.openConnection() .getInputStream());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    image.setImageBitmap(mIcon);
                                }
                            });


                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }









    }
}
