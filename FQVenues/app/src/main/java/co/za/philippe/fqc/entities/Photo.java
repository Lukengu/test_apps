/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.za.philippe.fqc.entities;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 *
 * @author p.lukengu
 */
public class Photo implements Serializable {
    private static final long serialVersionUID = 42L;

    private String id;
    private String url;
    private String createdAt;
    public String source;
    private String firstName;
    private String surname;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String toJSON() throws IllegalAccessException, JSONException {
        JSONObject object = new JSONObject();

        for(Field field  :  Photo.class.getDeclaredFields() ){
            object.put(field.getName(), field.get(this));
        };
        return object.toString();
    }
    

}
