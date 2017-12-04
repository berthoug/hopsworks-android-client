package io.hops.android.streams.hopsworks;


import com.google.gson.Gson;

public class HopsWorksResponse {

    private String message;

    private Integer code;

    private String reason;

    private String jwt;

    public HopsWorksResponse(String message, Integer code, String reason, String jwt) {
        this.message = message;
        this.code = code;
        this.reason = reason;
        this.jwt = jwt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public static HopsWorksResponse fromJson(String json){
        return new Gson().fromJson(json, HopsWorksResponse.class);
    }

}

