package com.global.ct.frameinventory.domain;

public class Location {

    private String country;
    private String region;
    private String town;
    private String postcode;
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer distanceToSchoolMetres;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getDistanceToSchoolMetres() {
        return distanceToSchoolMetres;
    }

    public void setDistanceToSchoolMetres(Integer distanceToSchoolMetres) {
        this.distanceToSchoolMetres = distanceToSchoolMetres;
    }
}
