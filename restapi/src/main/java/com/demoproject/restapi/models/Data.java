package com.demoproject.restapi.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {
    @SerializedName("data")
    private List<UsersDetails> usersDetails;

    public List<UsersDetails> getUsersDetails() {
        return usersDetails;
    }

    public void setUsersDetails(List<UsersDetails> usersDetails) {
        this.usersDetails = usersDetails;
    }
}
