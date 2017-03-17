package com.example.yevgeniy.makesensesrv;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yevgeniy on 18/02/17.
 */

enum ServiceState {IDLE, HAS_API_UUID, LOGIN, DEVICE_CHECK, DEVICE_REGISTER, GET_SENSORS};
enum SensorState {PUBLISH_IDLE, PUBLISH_SENSOR_LIST, PUBLISH_DATA};

public class MSServiceState implements Runnable {
    private static final String TAG = "MSService";
    Boolean isWorking = false;
    ServiceRequest serviceAPI;
    ServiceState state;
    SensorState getSensorState;
    boolean IsSensorState = false;

    MSServiceState(ServiceRequest serviceRequest) {
        serviceAPI = serviceRequest;
        isWorking = true;
        getSensorState = SensorState.PUBLISH_SENSOR_LIST;
    }

    public void SetState(ServiceState state) {
        this.state = state;
    }

    public void StopService() {
        isWorking = false;
    }

    @Override
    public void run() {
        while (isWorking){
            switch (state) {
                case IDLE:
                    if(IsSensorState) {
                        state = ServiceState.GET_SENSORS;
                    }

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case HAS_API_UUID:
                    Log.e(TAG, "[INFO] HAS_API_UUID");
                    String key = LocalRepo.settings.getString("UUID", "");
                    if (key == "") {
                        SetState(ServiceState.LOGIN);
                    } else {
                        SetState(ServiceState.DEVICE_CHECK);
                    }
                    break;
                case LOGIN:
                    Log.e(TAG, "[INFO] LOGIN");
                    IsSensorState = false;
                    serviceAPI.Login("ykiveish", "1234", new ServiceCallback() {
                        @Override
                        public void CallbackCall(String data) {
                            /*
                             * {"id":2,
                             * "key":"23e7797b-2a56-facb-ed46-6c61c4626698",
                             * "userName":"ykiveish",
                             * "password":"1234",
                             * "ts":1485622511,
                             * "lastLoginTs":1485622511,
                             * "enabled":1}
                             */
                            String key = null;
                            try {
                                JSONObject reader = new JSONObject(data);
                                key = reader.getString("key");
                                LocalRepo.IsServiceConnected = true;
                            } catch (JSONException e) {
                                Log.e(TAG, "[INFO] User credentials are incorrect.");
                                LocalRepo.IsServiceConnected = false;
                                System.exit(0);
                            }
                            Log.e(TAG, "[INFO] " + key);
                            LocalRepo.API_UUID = key;
                            LocalRepo.settings.edit().putString("UUID", key).commit();
                            SetState(ServiceState.DEVICE_CHECK);
                        }
                    });
                    state = ServiceState.IDLE;
                    break;
                case DEVICE_CHECK:
                    Log.e(TAG, "[INFO] DEVICE_CHECK");
                    IsSensorState = false;
                    serviceAPI.CheckDevice(new ServiceCallback() {
                        @Override
                        public void CallbackCall(String data) {
                            Log.e(TAG, "[INFO] " + data);
                            try {
                                JSONObject reader = new JSONObject(data);
                                int id = reader.getInt("id");
                                LocalRepo.DeviceID = id;
                                state = ServiceState.GET_SENSORS;
                            } catch (JSONException e) {
                                state = ServiceState.DEVICE_REGISTER;
                            }
                        }
                    });
                    state = ServiceState.IDLE;
                    break;
                case DEVICE_REGISTER:
                    Log.e(TAG, "[INFO] DEVICE_REGISTER");
                    IsSensorState = false;
                    serviceAPI.RegisterDevice(new ServiceCallback() {
                        @Override
                        public void CallbackCall(String data) {
                            Log.e(TAG, "[INFO - DEVICE_REGISTER] " + data);
                            state = ServiceState.DEVICE_CHECK;
                        }
                    });
                    state = ServiceState.IDLE;
                    break;
                case GET_SENSORS:
                    Log.e(TAG, "[INFO] GET_SENSORS");
                    IsSensorState = true;

                    switch (getSensorState) {
                        case PUBLISH_IDLE:
                            Log.e(TAG, "[INFO - SENSOR] PUBLISH_IDLE");
                            break;
                        case PUBLISH_SENSOR_LIST:
                            Log.e(TAG, "[INFO - SENSOR] PUBLISH_SENSOR_LIST");
                            break;
                        case PUBLISH_DATA:
                            Log.e(TAG, "[INFO - SENSOR] PUBLISH_DATA");

                            break;
                    }

                    state = ServiceState.IDLE;
                    break;
            }
        }
    }
}
