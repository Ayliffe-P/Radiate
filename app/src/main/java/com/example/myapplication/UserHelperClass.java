package com.example.myapplication;

public class UserHelperClass {
    String name, email, password;
    Boolean metricSystem;

    public UserHelperClass() { }

    public UserHelperClass( String name, String email, String password, Boolean metricSystem) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.metricSystem = metricSystem;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {this.password = password;}
    public Boolean getMetricSystem() {
        return metricSystem;
    }

    public void setMetricSystem(Boolean metricSystem) {this.metricSystem = metricSystem;}
}
