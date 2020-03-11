package com.example.geofencinglocation.retrofitsdk.response;

import com.example.geofencinglocation.retrofitsdk.model.ShortestDistancePojo;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ShortestDistanceResponse {
    @SerializedName("status")
    @Expose
    private long status;
    @SerializedName("response")
    @Expose
    private String response;
    @SerializedName("msg")
    @Expose
    private String msg;
    @SerializedName("data")
    @Expose
    private ShortestDistancePojo shortestDistancePojo;

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public ShortestDistancePojo getShortestDistancePojo() {
        return shortestDistancePojo;
    }

    public void setShortestDistancePojo(ShortestDistancePojo shortestDistancePojo) {
        this.shortestDistancePojo = shortestDistancePojo;
    }

}
