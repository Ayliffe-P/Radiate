package com.example.myapplication;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class Profile {

    private static Profile instance = null;

    private Profile(){};

    public static Profile getInstance(){
        if (instance == null) {
            instance = new Profile();
        }
        return instance;
    }
    String _name,_email,_password;
    DatabaseReference _reference;
    Boolean _googleAuth, _metricSystem;

    public static void setInstance(Profile instance) {
        Profile.instance = instance;
    }


    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public void setEmail(String email) {
        _email = email;
    }

    public String getPassword() {
        return _password;
    }

    public void setPassword(String password) {
        _password = password;
    }

    public DatabaseReference getReference() {
        return _reference;
    }

    public void setReference(DatabaseReference reference) {
        _reference = reference;
    }

    public Boolean getAuthStatus() {
        return _googleAuth;
    }
    public Boolean getMetricSystem() {
        return _metricSystem;
    }
    public void setMetricSystem(Boolean metricSystem) {
        _metricSystem = metricSystem;
    }

    public void setAuthStatus(Boolean googleAuth) {
        _googleAuth = googleAuth;
    }

    public void Clear(){
        _name = _email = _password ="";
        _reference = null;
        _googleAuth= false;
    }

}
