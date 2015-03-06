package com.plugin.gcm;

import org.json.JSONArray;

public interface AsyncRegistrationInterface{
    public boolean handleRegister(JSONArray data);
    public void onRegistrationSuccess(String registrationId);
    public void onRegistrationFailure(String errorId);
}
