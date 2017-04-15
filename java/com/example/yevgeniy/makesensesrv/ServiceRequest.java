package com.example.yevgeniy.makesensesrv;

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
    List<ServiceCallback> onPublishSensorListEvent;
    List<ServiceCallback> onPublishSensorDataEvent;

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

    class PublishSensorListHandler implements ServiceCallback {
        public void CallbackCall(String data) {
        }
    } PublishSensorListHandler publishSensorListCall = new PublishSensorListHandler();

    class PublishSensorDataHandler implements ServiceCallback {
        public void CallbackCall(String data) {
        }
    } PublishSensorDataHandler publishSensorDataCall = new PublishSensorDataHandler();

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
        AddRequest(new HttpRequest("GET", LocalRepo.HTTP_SERVER + "/insert/device/" + LocalRepo.API_UUID + "/2/" + LocalRepo.DeviceUUID + "/android/4.3.1/SamsungGalaxyS3", "", false, onRegisterDeviceEvent));
    }

    public void PublishCameraSensor (int type, ServiceCallback callback) {
        if (onPublishSensorListEvent == null) {
            onPublishSensorListEvent = new ArrayList<ServiceCallback>();
        } else {
            onPublishSensorListEvent.clear();
        }

        onPublishSensorListEvent.add(publishSensorListCall);
        onPublishSensorListEvent.add(callback);
        AddRequest(new HttpRequest("GET", LocalRepo.HTTP_SERVER + "/insert/sensor/camera" + "/" + LocalRepo.API_UUID + "/" + LocalRepo.DeviceUUID + "/" + type, "", false, onPublishSensorListEvent));
    }

    public void PublishCameraSensorImage (CameraSensor camera, ServiceCallback callback) {
        if (onPublishSensorDataEvent == null) {
            onPublishSensorDataEvent = new ArrayList<ServiceCallback>();
        } else {
            onPublishSensorDataEvent.clear();
        }

        onPublishSensorDataEvent.add(publishSensorDataCall);
        onPublishSensorDataEvent.add(callback);

        HttpRequest request = new HttpRequest("POST", LocalRepo.HTTP_SERVER + "/update/sensor/camera/image/" + LocalRepo.API_UUID + "/" + LocalRepo.DeviceUUID + "/" + camera.CameraType, "", false, onPublishSensorDataEvent);
        request.SetStreamPublish(true);
        request.StreamBuffer = camera.Buffer;
        AddRequest(request);
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

                if (req.GetStreamPublish() == true) {
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);

                    urlConnection.setRequestProperty("Connection", "Keep-Alive");
                    urlConnection.setRequestProperty("Cache-Control", "no-cache");

                    urlConnection.setReadTimeout(35000);
                    urlConnection.setConnectTimeout(35000);

                    Log.e(TAG, "[INFO] StreamBuffer size = " + req.StreamBuffer.length);
                    OutputStream os = urlConnection.getOutputStream();
                    os.write(req.StreamBuffer);
                    os.flush(); os.close();

                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(in));
                    String line = "";
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((line = responseStreamReader.readLine()) != null)
                        stringBuilder.append(line).append("\n");
                    responseStreamReader.close();
                } else {
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }
                    dataStr = buffer.toString();
                }

                for (ServiceCallback listener : req.callbacks) {
                    listener.CallbackCall(dataStr);
                }

                urlConnection.disconnect();
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
