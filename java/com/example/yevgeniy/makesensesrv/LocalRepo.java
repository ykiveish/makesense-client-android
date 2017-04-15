package com.example.yevgeniy.makesensesrv;

import android.app.Activity;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

import java.util.List;

/**
 * Created by yevgeniy on 06/02/17.
 */

public class LocalRepo {
    static SharedPreferences settings = null;
    static TelephonyManager telephony = null;
    static String HTTP_SERVER = "http://ec2-35-161-108-53.us-west-2.compute.amazonaws.com:8080";
    static String API_UUID = "";
    static String DeviceUUID = "";
    static int DeviceID = 0;
    static Boolean IsServiceConnected = false;

    static byte[] CameraFronImageBuffer;
    static byte[] CameraRearImageBuffer;

    static List SensorsList = null;
    static int PublishedSensorCount = 0;

    static Boolean GetDeviceUUID () {
        if (telephony == null) return false;
        else {
            LocalRepo.DeviceUUID = LocalRepo.telephony.getDeviceId();
            return true;
        }
    }
}
