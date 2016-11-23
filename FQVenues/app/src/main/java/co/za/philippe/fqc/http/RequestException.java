package co.za.philippe.fqc.http;

import org.json.JSONObject;

public class RequestException extends Exception {

    private static final long serialVersionUID = 3519935485837012006L;

    public RequestException(String message) {
        super(message);
    }

    public RequestException(JSONObject jsonObject) {
        // TODO Auto-generated constructor stub
    }

    public RequestException() {
        super("RESTException");
    }

}
