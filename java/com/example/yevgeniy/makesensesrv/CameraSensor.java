package com.example.yevgeniy.makesensesrv;

import android.hardware.Camera;
import android.util.Log;

import java.util.Arrays;

/**
 * Created by yevgeniy on 18/03/17.
 */

public class CameraSensor extends AbstractSensor implements Runnable {
    private static final String TAG = "CameraSensor";
    public int CameraType = 1;
    public String CameraName = "FRONT";
    private int _androidCameraAPIType;
    private int _cameraId;
    private boolean _isWorking;
    private Camera _camera;
    public byte[] Buffer;

    CameraSensor (int type, String name) {
        this.SensorType = 1;
        this.SensorName = "Camera";

        this.CameraName = name;
        /* Note: FRONT_CAMERA = 1, REAR_CAMERA = 2 */
        this.CameraType = type;

        if (this.CameraType == 1) {
            this._androidCameraAPIType = Camera.CameraInfo.CAMERA_FACING_FRONT;
            this.Buffer = LocalRepo.CameraFronImageBuffer;
        } else if (this.CameraType == 2) {
            this._androidCameraAPIType = Camera.CameraInfo.CAMERA_FACING_BACK;
            this.Buffer = LocalRepo.CameraRearImageBuffer;
        } else {
            this._androidCameraAPIType = Camera.CameraInfo.CAMERA_FACING_FRONT;
            this.Buffer = LocalRepo.CameraFronImageBuffer;
        }

        this._cameraId = this._getCameraID();
        this._isWorking = false;
    }

    private int _getCameraID () {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int camIdx = 0; camIdx < Camera.getNumberOfCameras(); camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == this._androidCameraAPIType) {
                try {
                    return camIdx;
                } catch (RuntimeException e) { }
            }
        }
        return -1;
    }

    private Camera.PictureCallback _onPictureHandler = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Buffer = Arrays.copyOf(data, data.length);
            Log.e(TAG, "[INFO] Image size = " + Buffer.length);
        }
    };

    private void _initCamera () {
        if (this._cameraId != -1) {
            this._camera = Camera.open(this._cameraId);
            Camera.Parameters parameters = this._camera.getParameters();

            this._camera.setParameters(parameters);
            this._camera.startPreview();
        }
    }

    private void _getImage () {
        if (this._cameraId != -1) {
            this._camera.takePicture(null, null, this._onPictureHandler);
        }
    }

    public void StopCapturing () {
        this._isWorking = false;
    }

    public boolean IsActive () {
        return this._isWorking;
    }

    @Override
    public void run() {
        this._isWorking = true;
        this._initCamera();
        while (_isWorking) {
            this._getImage();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        this._camera.stopPreview();
        this._camera.release();
        this._camera = null;
    }
}
