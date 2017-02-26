package com.example.yevgeniy.makesensesrv;

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by yevgeniy on 05/02/17.
 */

public class ServiceRequest implements Runnable {
    private static final String TAG = "ServiceRequest";

    URL url;
    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;
    String dataStr = null;
    Boolean serviceWorking = false;

    List<ServiceCallback> onLoginEvent;
    List<ServiceCallback> onCheckDeviceEvent;
    List<ServiceCallback> onRegisterDeviceEvent;

    BlockingQueue queue = new LinkedBlockingQueue();

    ServiceRequest () {
        serviceWorking = true;
    }

    public void StopService () {
        serviceWorking = false;
    }

    class LoginHandler implements ServiceCallback {
        public void CallbackCall(String data) {
        }
    } LoginHandler loginCall = new LoginHandler();

    class CheckDeviceHandler implements ServiceCallback {
        public void CallbackCall(String data) {
        }
    } CheckDeviceHandler checkDeviceCall = new CheckDeviceHandler();

    class RegisterDeviceHandler implements ServiceCallback {
        public void CallbackCall(String data) {
        }
    } RegisterDeviceHandler registerDeviceCall = new RegisterDeviceHandler();

    public void Login (String user, String password, ServiceCallback callback) {
        if (onLoginEvent == null) {
            onLoginEvent = new ArrayList<ServiceCallback>();
        } else {
            onLoginEvent.clear();
        }

        onLoginEvent.add(loginCall);
        onLoginEvent.add(callback);
        AddRequest(new HttpRequest("GET", LocalRepo.HTTP_SERVER + "/login/" + user + "/" + password, "", false, onLoginEvent));
    }

    public void CheckDevice (ServiceCallback callback) {
        if (onCheckDeviceEvent == null) {
            onCheckDeviceEvent = new ArrayList<ServiceCallback>();
        } else {
            onCheckDeviceEvent.clear();
        }

        onCheckDeviceEvent.add(checkDeviceCall);
        onCheckDeviceEvent.add(callback);
        AddRequest(new HttpRequest("GET", LocalRepo.HTTP_SERVER + "/select/device/" + LocalRepo.API_UUID + "/" + LocalRepo.DeviceUUID, "", false, onCheckDeviceEvent));
    }

    public void RegisterDevice (ServiceCallback callback) {
        if (onRegisterDeviceEvent == null) {
            onRegisterDeviceEvent = new ArrayList<ServiceCallback>();
        } else {
            onRegisterDeviceEvent.clear();
        }

        onRegisterDeviceEvent.add(registerDeviceCall);
        onRegisterDeviceEvent.add(callback);
        AddRequest(new HttpRequest("GET", LocalRepo.HTTP_SERVER + "/insert/device/" + LocalRepo.API_UUID + "/2/" + LocalRepo.DeviceUUID + "/android/4.3.1", "", false, onRegisterDeviceEvent));
    }

    public void AddRequest(HttpRequest req) {
        queue.add(req);
    }

    public void run() {
        try {
            while (serviceWorking) {
                HttpRequest req = (HttpRequest) queue.take();

                url = new URL(req.server);
                Log.e(TAG, "[INFO] Opening connection ...");
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod(req.type);
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                dataStr = buffer.toString();

                for (ServiceCallback listener : req.callbacks) {
                    listener.CallbackCall(dataStr);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                Log.e(TAG, "[INFO] Closing connection ...");
                urlConnection.disconnect();
            }
        }
    }
}
