package com.plugin.gcm;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

/**
* Push Notifications Plugin
*/
public class PushPlugin extends CordovaPlugin implements AsyncRegistrationInterface {

  public static final String TAG = "PushPlugin";

  public static final String REGISTER = "register";

  public static final String UNREGISTER = "unregister";

  public static final String EXIT = "exit";

  public static final String ON_MESSAGE_FOREGROUND = "onMessageInForeground";

  public static final String ON_MESSAGE_BACKGROUND = "onMessageInBackground";

  public static final String SENDER_ID = "senderID";

  public static final String GCM_SENDER_ID = "gcm_senderid";

  private CallbackContext registrationCallback;


  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    LOG.setLogLevel(LOG.VERBOSE);

    readSenderIdFromCordovaConfig();
  }

  private void readSenderIdFromCordovaConfig() {
    Bundle extras = cordova.getActivity().getIntent().getExtras();
    if(extras.containsKey(GCM_SENDER_ID)) {
      String senderID = extras.getString(GCM_SENDER_ID);
      NotificationService
      .getInstance(getApplicationContext())
      .setSenderID(senderID);
    }
  }

  /**
  * Gets the application context from cordova's main activity.
  *
  * @return the application context
  */
  private Context getApplicationContext() {
    return this.cordova.getActivity().getApplicationContext();
  }
    @Override
    public boolean handleRegister(JSONArray data) {
    try {

        //Log.v(TAG,
        //      "Handling registration on separate thread ->" + data.toString());

      JSONObject jo = data.getJSONObject(0);

      String senderID = (String) jo.get(SENDER_ID);

      Context appContext = getApplicationContext();

      NotificationService service = NotificationService.getInstance(appContext);

      if(senderID != null && senderID.trim().length() > 0) {
        service.setSenderID(senderID);
      }

      service.registerWebView(this.webView);

      service.addRegistrationHandler(this.webView,this);

      return true;

    }
    catch (Exception e) {
      Log.e(TAG, "execute: Got JSON Exception " + e.getMessage());
      this.registrationCallback.error(e.getMessage());
      return false;
    }
  }

    class RegistrationRunnable implements Runnable{
        private JSONArray data;
        private AsyncRegistrationInterface iFace;

        RegistrationRunnable(JSONArray data, AsyncRegistrationInterface iFace){
            this.data = data;
            this.iFace = iFace;

            Log.v(TAG, "iFace = " + iFace.toString());
        }

        public void run(){
            iFace.handleRegister(this.data);
        }
    }

  @Override
  public boolean execute(String action, JSONArray data, CallbackContext callbackContext) {

    boolean result = false;

    if (REGISTER.equals(action)) {

        Log.v(TAG, "handleRegister -> data: " + data);

        Log.v(TAG, "PushPlugin == " + this.toString());

        //need to keep a reference hanging around to the callback
        this.registrationCallback = callbackContext;

        PluginResult temp = new PluginResult(PluginResult.Status.NO_RESULT);

        temp.setKeepCallback(true);

        this.registrationCallback.sendPluginResult(temp);

        //this.cordova.getThreadPool()
        //    .execute(new RegistrationRunnable(data,this));

        result = handleRegister(data);
    }
    else if (ON_MESSAGE_FOREGROUND.equals(action)) {

      result = handleOnMessageForeground(data, callbackContext);

    }
    else if (ON_MESSAGE_BACKGROUND.equals(action)) {

      result = handleOnMessageBackground(data, callbackContext);

    }
    else if (UNREGISTER.equals(action)) {

      result = handleUnRegister(data, callbackContext);

    }
    else {
      result = false;
      Log.e(TAG, "Invalid action : " + action);
      callbackContext.error("Invalid action : " + action);
    }

    Log.v(TAG, "Exiting Exec method: " + this.toString());
    return result;
  }



  @Override
  public void onRegistrationSuccess(String registrationId){

      final resultString = registrationID;
      Log.v(TAG, "Registration Success called: "
            + registrationId
            + " for instance WebView "
            + this.webView.toString()
            + " callBack "
            + this.registrationCallback.getCallbackId());

      Log.v(TAG, "CallbackContext Finished? "
            + this.registrationCallback.isFinished());


      Log.v(TAG, "CallbackContext is changing threads? "
            + this.registrationCallback.isChangingThreads());

      this.cordova.getActivity().runOnUiThread(new Runnable(){
              public void run(){
                  PushPlugin.this.webView.setNetworkAvailable(true);

                  PluginResult success =
                      new PluginResult(PluginResult.Status.OK, resultString);
                  success.setKeepCallback(false);
                  PushPlugun.this.registrationCallback.sendPluginResult(success);
              };
          });
  }



  @Override
  public void onRegistrationFailure(String errorId){

      final resultString = errorId;
      Log.v(TAG, "Registration Failure called: "
            + errorId
            + " for instance WebView "
            + this.webView.toString()
            + " callBack "
            + this.registrationCallback.getCallbackId());

      Log.v(TAG, "CallbackContext Finished? "
            + this.registrationCallback.isFinished());

      Log.v(TAG, "CallbackContext is changing threads? "
            + this.registrationCallback.isChangingThreads());

      this.cordova.getActivity().runOnUiThread(new Runnable(){
              public void run(){
                  PushPlugin.this.webView.setNetworkAvailable(true);

                  PluginResult success =
                      new PluginResult(PluginResult.Status.OK, resultString);
                  success.setKeepCallback(false);
                  PushPlugin.this.registrationCallback.sendPluginResult(success);
              };
          });

      //this.webView.setNetworkAvailable(true);

      //PluginResult error =
      //    new PluginResult(PluginResult.Status.ERROR, errorId);
      //error.setKeepCallback(false);
      //this.registrationCallback.sendPluginResult(error);
  }

  private boolean handleUnRegister(JSONArray data, CallbackContext callbackContext) {
    Log.v(TAG, "handleUnRegister() -> data: " + data);

    NotificationService
    .getInstance(getApplicationContext())
    .unRegister();

    callbackContext.success();
    return true;
  }

  private boolean handleOnMessageForeground(JSONArray data, CallbackContext callbackContext) {
    Log.v(TAG, "handleOnMessageForeground() -> data: " + data);

    NotificationService
    .getInstance(getApplicationContext())
    .addNotificationForegroundCallBack(this.webView, callbackContext);

    return true;
  }

  private boolean handleOnMessageBackground(JSONArray data, CallbackContext callbackContext) {
    Log.v(TAG, "handleOnMessageBackground() -> data: " + data);

    NotificationService
    .getInstance(getApplicationContext())
    .addNotificationBackgroundCallBack(this.webView, callbackContext);

    return true;
  }

  @Override
  public void onPause(boolean multitasking) {
    super.onPause(multitasking);

    Log.v(TAG, "onPause() -> webView: " + webView);

    NotificationService
    .getInstance(getApplicationContext())
    .setForeground(false);
  }

  @Override
  public void onResume(boolean multitasking) {
    super.onResume(multitasking);

    Log.v(TAG, "onResume() -> webView: " + webView);

    NotificationService
    .getInstance(getApplicationContext())
    .setForeground(true);
  }


  public void onDestroy() {

    Log.v(TAG, "onDestroy() -> webView: " + webView);

    NotificationService
    .getInstance(getApplicationContext())
    .removeWebView(this.webView);

    super.onDestroy();
  }
}
