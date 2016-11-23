/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.za.philippe.fqc;

import co.za.philippe.fqc.entities.Photo;
import co.za.philippe.fqc.entities.Venue;
import co.za.philippe.fqc.http.Request;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author p.lukengu
 */
public class Api extends Request {

    private String api_call;

    public Api() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        api_call = ("https://api.foursquare.com/v2/" + "%s" + "?client_id=" + Config.CLIENT_ID);
        api_call += "&client_secret=" + Config.CLIENT_SECRET;
        api_call += "%s";
        api_call += "&v=" + sdf.format(Calendar.getInstance().getTime());

    }

    @Override
    protected Request buildUrl(String module, Map params) {

        StringBuilder sb = new StringBuilder();
        Set paramsSet = params.entrySet();
        Iterator iterator = paramsSet.iterator();
        String api_call = "";

        if (params != null) {
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                sb.append("&").append(entry.getKey().toString()).append("=").append(entry.getValue().toString());
            }
        }

        api_call = String.format(this.api_call, new Object[]{module, sb.toString()});
        this.apiurl = api_call.concat(sb.toString());
        return this;

    }

    public ArrayList getVenues(double lat, double lng) throws IOException, JSONException {
        Map params = new HashMap();
        ArrayList venues = new ArrayList();
        params.put("ll", String.valueOf(lat) + "," + String.valueOf(lng));
        buildUrl("venues/search", params).execute("");

        JSONObject jsnobj = getObjectResponse();
        JSONArray jsonarr = jsnobj.getJSONObject("response").getJSONArray("venues");
        for (int i = 0; i < jsonarr.length(); i++) {
            JSONObject o = jsonarr.getJSONObject(i);

            String cover = coverUrl(o.getString("id"));
            if (cover.length() != 0) {
                Venue venue = new Venue();
                venue.setId(o.getString("id"));
                venue.setDistance(o.getJSONObject("location").getInt("distance"));
                venue.setLatitude(o.getJSONObject("location").getDouble("lat"));
                venue.setLongitude(o.getJSONObject("location").getDouble("lng"));
                venue.setDisplayPhoto(cover);
                venues.add(venue);
            }

        }
        return venues;
    }

    private String coverUrl(String venueId) throws JSONException, IOException {
        buildUrl("venues/" + venueId + "/photos", new HashMap()).execute("");
        JSONObject jsnobj = getObjectResponse();
        Random random = new Random();
        int count = jsnobj.getJSONObject("response").getJSONObject("photos").getInt("count");
        if (count != 0) {
            JSONArray items = jsnobj.getJSONObject("response").getJSONObject("photos").getJSONArray("items");
            int i = random.nextInt(count);
            JSONObject o = items.getJSONObject(i);
            return o.getString("prefix").concat("width960").concat(o.getString("suffix"));
        }
        return "";
    }

    public ArrayList<Photo> getPhotos(String venueId) throws JSONException, IOException {

        ArrayList<Photo> photos = new ArrayList<Photo>();

        buildUrl("venues/" + venueId + "/photos", new HashMap()).execute("");

        JSONObject jsnobj = getObjectResponse();
        JSONArray jsonarr = jsnobj.getJSONObject("response").getJSONObject("photos").getJSONArray("items");
        for (int i = 0; i < jsonarr.length(); i++) {
            JSONObject o = jsonarr.getJSONObject(i);
            Photo photo = new Photo();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
            photo.setId(o.getString("id"));
            // photo.setCreatedAt( (long) o.getInt("createdAt") * 1000 );
            Date d = new Date((long) o.getInt("createdAt") * 1000);
            photo.setCreatedAt(sdf.format(d));
            photo.setUrl(o.getString("prefix").concat("width960").concat(o.getString("suffix")));
            photo.setSource(o.getJSONObject("source").getString("name"));
            photo.setFirstName(o.getJSONObject("user").getString("firstName"));
            photo.setSurname(o.getJSONObject("user").optString("lastName"));
            // photo.setSurname(o.getJSONObject("user").getString("lastName"));
            photos.add(photo);
        }

        return photos;

    }
}
