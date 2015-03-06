package com.plugin.gcm;

public interface AsyncRegistrationInterface{
    public void onRegistrationSuccess(String registrationId);
    public void onRegistrationFailure(String errorId);
}
