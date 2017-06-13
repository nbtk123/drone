package com.dji.sdkdemo.BrainControl;

import android.util.Log;

import dji.sdk.api.Camera.DJICameraSettingsTypeDef;
import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIError;
import dji.sdk.interfaces.DJIExecuteResultCallback;

/**
 * Created by nbtk123 on 06/06/2017.
 */

public class CameraControllerImpl implements CameraController {

    private static final String TAG = CameraControllerImpl.class.getSimpleName();

    @Override
    public void takePicture() {
        DJIDrone.getDjiCamera().startTakePhoto(DJICameraSettingsTypeDef.CameraCaptureMode.Camera_Single_Capture, new DJIExecuteResultCallback(){

            @Override
            public void onResult(DJIError mErr)
            {
                // TODO Auto-generated method stub
                Log.d(TAG, "Set Action errorCode = "+ mErr.errorCode);
                Log.d(TAG, "Set Action errorDescription = "+ mErr.errorDescription);
                //String result = "errorCode =" + mErr.errorCode + "\n"+"errorDescription =" + DJIError.getErrorDescriptionByErrcode(mErr.errorCode);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                DJIDrone.getDjiCamera().stopTakePhoto(new DJIExecuteResultCallback(){

                    @Override
                    public void onResult(DJIError mErr)
                    {
                        // TODO Auto-generated method stub
                        Log.d(TAG, "Stop Takephoto errorCode = "+ mErr.errorCode);
                        Log.d(TAG, "Stop Takephoto errorDescription = "+ mErr.errorDescription);
                        //String result = "errorCode =" + mErr.errorCode + "\n"+"errorDescription =" + DJIError.getErrorDescriptionByErrcode(mErr.errorCode);
                    }

                });
            }
        });
    }

    @Override
    public void startVideo() {
        DJIDrone.getDjiCamera().startRecord(new DJIExecuteResultCallback(){

            @Override
            public void onResult(DJIError mErr)
            {
                // TODO Auto-generated method stub
                Log.d(TAG, "Start Recording errorCode = "+ mErr.errorCode);
                Log.d(TAG, "Start Recording errorDescription = "+ mErr.errorDescription);
                //String result = "errorCode =" + mErr.errorCode + "\n"+"errorDescription =" + DJIError.getErrorDescriptionByErrcode(mErr.errorCode);
            }

        });
    }

    @Override
    public void stopVideo() {
        DJIDrone.getDjiCamera().stopRecord(new DJIExecuteResultCallback(){

            @Override
            public void onResult(DJIError mErr)
            {
                // TODO Auto-generated method stub
                Log.d(TAG, "Stop Recording errorCode = "+ mErr.errorCode);
                Log.d(TAG, "Stop Recording errorDescription = "+ mErr.errorDescription);
                //String result = "errorCode =" + mErr.errorCode + "\n"+"errorDescription =" + DJIError.getErrorDescriptionByErrcode(mErr.errorCode);
            }

        });
    }
}
