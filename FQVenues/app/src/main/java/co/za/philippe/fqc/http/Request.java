package co.za.philippe.fqc.http;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class Request {

    public static String APIURL = "";
    public static String APIKEY = "";
    protected String apiurl;
    public String controller;

    private String response;

    public String mnodule = "";
    // private final static Pattern LTRIM = Pattern.compile("^\\s+");
    private final static Pattern REMOVEAMP = Pattern.compile("\\&+$");
    private final static Pattern REMOVESLASH = Pattern.compile("\\/+$");

    public enum Action {
        POST("POST"), GET("GET"), PUT("PUT"), DELETE("DELETE");
        private String value;

        private Action(String value) {
            this.value = value;
        }
    };

    public Action action = Action.GET;

    protected Request buildUrl(String module, boolean auth) {
        return this;
    }

    protected Request buildUrl(String module, Map params) {
        return this;
    }

    protected Request buildUrl(String module) {
        return buildUrl(module, 0, "", null);

    }

    protected Request buildUrl(String module, int id) {
        return buildUrl(module, id, "", null);

    }

    protected Request buildUrl(String module, int id, String call) {
        return buildUrl(module, id, call, null);

    }

    protected Request buildUrl(String module, int id, String sub, HashMap params) {
        String api_call = new StringBuilder(APIURL).append(module).toString();
        if (id > 0) {
            api_call = api_call.concat("/").concat(String.valueOf(id));
        }
        if (sub.length() > 0) {
            api_call = api_call.concat("/").concat(sub);
        }
        if (params != null && !params.isEmpty()) {
            Iterator iterator = params.entrySet().iterator();
            String urlParams = "";
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();

                urlParams += entry.getKey().toString().concat("/")
                        .concat(entry.getValue().toString())
                        .concat("/");
            }
            urlParams = REMOVESLASH.matcher(urlParams).replaceAll("");
            api_call = api_call.concat(urlParams);
        }
        this.apiurl = api_call;
        System.out.println(this.apiurl);
        return this;
    }

    

    public void execute(String input) throws IOException {
        String response = "";

        HttpURLConnection connection = getConnection(action.value);
        if (action.equals(Action.POST) || action.equals(Action.PUT)) {
            System.out.println("POST  value:" + input);
            connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            os.write(input.getBytes());
            os.flush();
            System.out.println(input);
        } else {
            connection.setUseCaches(false);
        }
        int responseCode = connection.getResponseCode();
        System.out.println(responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
            String output;
            StringBuilder api_response = new StringBuilder();
            while ((output = br.readLine()) != null) {
                api_response.append(output);
            }
            response = api_response.toString();
            //return response.toString();
        } else {
            HashMap result = new HashMap();
            if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                result.put("message", "204 Executed Succesfully");
                JSONObject json = new JSONObject(result);
                response = json.toString();
            }

            if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                result.put("message", "500 Internal Server Error");
                JSONObject json = new JSONObject(result);
                response = json.toString();
            }
        }
        connection.disconnect();
        setResponse(response);
    }

    /**
     *
     * @param method
     * @return
     * @throws IOException
     */
    private HttpURLConnection getConnection(String method) throws IOException {

        System.out.println("Action value:" + method);
        System.out.println("URL  value:" + this.apiurl);
        URL obj = new URL(this.apiurl);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod(method);
        //con.setRequestProperty("Content-Type",
        //"application/x-www-form-urlencoded");
        con.setRequestProperty("Content-Type",
                "application/json");
        if (APIKEY.length() > 0) {
            con.setRequestProperty("apikey", APIKEY);
        }
        con.setRequestProperty("Accept", "text/json");
        con.setUseCaches(false);

        return con;

    }

    private void setResponse(String response) {
        this.response = response;
    }

    public String getStringResponse() {
        return response;
    }

    public JSONArray getArrayResponse() throws JSONException {
        System.out.println(response);
        JSONArray json;
        json = new JSONArray(response);
        return json;
    }

    public JSONObject getObjectResponse() throws JSONException {

        System.out.println(response);
        JSONObject json;
        json = new JSONObject(response);
        return json;
    }

    /**
     *
     * @param response
     * @return
     * @throws JSONException
     * @throws
     */
    private List<HashMap<Object, Object>> getResponseResult(String response)
            throws JSONException, RequestException {

        JSONObject jsonObject = new JSONObject(response);

        if (jsonObject.getInt("HTTP_RESPONSE_CODE") == 200) {
            return parseJSONObject(response);
        } else {
            throw new RequestException("Error :"
                    + jsonObject.getInt("HTTP_RESPONSE_CODE") + " \n Message: "
                    + jsonObject.getString("HTTP_RESPONSE_MESSAGE"));
        }

    }

    protected List<HashMap<Object, Object>> parseJSONObject(String jsonString)
            throws JSONException {
        List<HashMap<Object, Object>> result = new ArrayList<HashMap<Object, Object>>();
        JSONObject jsonObject = new JSONObject(jsonString);
        Iterator<?> i = jsonObject.keys();
        HashMap<Object, Object> infos = new HashMap<Object, Object>();
        while (i.hasNext()) {
            Object key = i.next();

            if (TypeValidate.isInt(key)) {
                HashMap<Object, Object> map = new HashMap<Object, Object>();
                JSONObject jsonResponse = jsonObject.getJSONObject(key
                        .toString());

                Iterator<?> resultIterator = jsonResponse.keys();

                while (resultIterator.hasNext()) {
                    Object k = resultIterator.next();
                    map.put(k.toString(), jsonResponse.get(k.toString()));

                }
                result.add(map);
            } else {
                infos.put(key.toString(), jsonObject.get(key.toString()));
                result.add(infos);
            }

        }
        return result;

    }

}
