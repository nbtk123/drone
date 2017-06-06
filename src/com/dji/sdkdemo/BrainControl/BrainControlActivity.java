package com.dji.sdkdemo.BrainControl;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dji.sdkdemo.BrainControl.udp.DroneOrderParser;
import com.dji.sdkdemo.R;

import java.util.Timer;
import java.util.TimerTask;

import dji.sdk.api.DJIDrone;
import dji.sdk.api.GroundStation.DJIGroundStationMissionPushInfo;
import dji.sdk.api.MainController.DJIMainControllerSystemState;
import dji.sdk.interfaces.DJIGroundStationMissionPushInfoCallBack;
import dji.sdk.interfaces.DJIMcuUpdateStateCallBack;
import dji.sdk.interfaces.DJIReceivedVideoDataCallBack;
import dji.sdk.widget.DjiGLSurfaceView;

public class BrainControlActivity extends Activity implements View.OnClickListener, View.OnTouchListener {

    private static final String TAG = BrainControlActivity.class.getSimpleName();

    public static final int REQUEST_CODE_ENABLE_BT = 1;

    protected Button btnStart;
    protected Button btnEnd;
    protected Button btnIncreaseAlt;
    protected Button btnDecreaseAlt;

    protected Button btnForward;
    protected Button btnBackward;
    protected Button btnLeft;
    protected Button btnRight;

    protected DjiGLSurfaceView mDjiGLSurfaceView;
    protected TextView mHotPointTextView;
    protected TextView mConnectStateTextView;

    FollowMeController fmController;
    //BTController btController;
    JoystickController joystickController;
    CameraController cameraController;
    //UDPReceiver udpReceiver;
    protected BTServer btServer;

    protected Timer mTimer;

    protected static final int NAVI_MODE_HOT_POINT = 2;
    protected static final int NAVI_MODE_ATTITUDE = 0;

    private String HpInfoString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brain_control_layout);

        mDjiGLSurfaceView = (DjiGLSurfaceView)findViewById(R.id.DjiSurfaceView_gs);
        btnStart = (Button)findViewById(R.id.btnStart);
        btnEnd = (Button)findViewById(R.id.btnEnd);
        mHotPointTextView = (TextView) findViewById(R.id.HotPointInfoTV);
        mConnectStateTextView = (TextView)findViewById(R.id.ConnectStateGsTextView);
        btnIncreaseAlt = (Button)findViewById(R.id.btnIncreaseAlt);
        btnDecreaseAlt = (Button)findViewById(R.id.btnDecreaseAlt);

        btnForward = (Button) findViewById(R.id.btnForward);
        btnBackward = (Button) findViewById(R.id.btnBackward);
        btnLeft = (Button) findViewById(R.id.btnLeft);
        btnRight = (Button) findViewById(R.id.btnRight);
        btnForward.setOnTouchListener(this);
        btnBackward.setOnTouchListener(this);
        btnLeft.setOnTouchListener(this);
        btnRight.setOnTouchListener(this);

        fmController = new FollowMeController(this);

        //btController = new BTController(this);
        final DroneOrderParser droneOrderParser = new DroneOrderParser(new DroneOrderParser.ParserListener() {
            @Override
            public void onPitch(int value) {
                joystickController.setPitch(value);
            }

            @Override
            public void onRoll(int value) {
                joystickController.setRoll(value);
            }

            @Override
            public void onThrottle(int value) {
                joystickController.setThrottle(value);
            }

            @Override
            public void onYaw(int value) {
                joystickController.setYaw(value);
            }

            @Override
            public void takePicture() {
                cameraController.takePicture();
            }

            @Override
            public void startVideo() {
                cameraController.startVideo();
            }

            @Override
            public void stopVideo() {
                cameraController.stopVideo();
            }
        });
        btServer = new BTReceiverThread(BluetoothAdapter.getDefaultAdapter(), new BTListener() {
            @Override
            public void onBTDataReceived(String data) {
                Log.d(TAG, "received from BT: " + data);

                try {
                    droneOrderParser.parse(data);
                } catch (Exception e) {
                    Log.d(TAG, "error parsing received data from BT: " + data);
                    e.printStackTrace();
                }
            }
        });

        joystickController = new JoystickControllerImpl();
        cameraController = new CameraControllerImpl();

        /*if (btController.setup(this)) {
            btController.startBTServer();
        }*/

        mDjiGLSurfaceView.start();

        DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(new DJIReceivedVideoDataCallBack(){

            @Override
            public void onResult(byte[] videoBuffer, int size)
            {
                // TODO Auto-generated method stub
                mDjiGLSurfaceView.setDataToDecoder(videoBuffer, size);
            }


        });

        DJIDrone.getDjiMC().setMcuUpdateStateCallBack(new DJIMcuUpdateStateCallBack() {

            @Override
            public void onResult(DJIMainControllerSystemState state)
            {
                // TODO Auto-generated method stub
                double latitude = state.homeLocationLatitude;
                double longitude = state.homeLocationLongitude;

                if(latitude != -1 && longitude != -1 && latitude != 0 && longitude != 0){
                    fmController.setGetHomePiontFlag(true);
                }
                else{
                    fmController.setGetHomePiontFlag(false);
                }

            }

        });

        DJIDrone.getDjiGroundStation().setGroundStationMissionPushInfoCallBack(new DJIGroundStationMissionPushInfoCallBack() {

            @Override
            public void onResult(DJIGroundStationMissionPushInfo info) {
                // TODO Auto-generated method stub
                StringBuffer sb = new StringBuffer();
                switch(info.missionType.value()) {
                    case NAVI_MODE_HOT_POINT : {
                        sb.append("Mission Type : " + info.missionType.toString()).append("\n");
                        sb.append("Mission Status : " + info.hotPointMissionStatus).append("\n");
                        sb.append("Hot Point Radius : " + info.hotPointRadius).append("\n");
                        sb.append("Hot Point Angle : " + info.hotPointAngle).append("\n");
                        break;
                    }

                    case NAVI_MODE_ATTITUDE : {
                        sb.append("Mission Type : " + info.missionType.toString()).append("\n");
                        sb.append("Mission Reserve : " + info.reserved).append("\n");
                        break;
                    }

                    default :
                        sb.append("Worng Selection").append("\n");
                }

                HpInfoString = sb.toString();

                BrainControlActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        mHotPointTextView.setText(HpInfoString);
                    }

                });
            }

        });

        fmController.startLocationUpdate();

        if (btServer.askForBT(this)) {
            btServer.start();
        }
        /*udpReceiver = new UDPReceiver(new DroneOrderParser(), joystickController);
        udpReceiver.start();*/
    }

    @Override
    protected void onResume() {
        mTimer = new Timer();
        Task task = new Task();
        mTimer.schedule(task, 0, 500);

        DJIDrone.getDjiMC().startUpdateTimer(1000);

        super.onResume();
    }

    @Override
    protected void onPause() {
        if(mTimer!=null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }

        super.onPause();
        DJIDrone.getDjiMC().stopUpdateTimer();
    }

    @Override
    protected void onDestroy() {
        if(DJIDrone.getDjiCamera() != null) {
            DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(null);
        }
        mDjiGLSurfaceView.destroy();
        fmController.stopLocationUpdates();
        //btController.stopBTServer();
        //udpReceiver.interrupt();
        btServer.shutdown();
        super.onDestroy();
    }

    private void checkConnectState(){

        BrainControlActivity.this.runOnUiThread(new Runnable(){

            @Override
            public void run()
            {
                if(DJIDrone.getDjiCamera() != null){
                    boolean bConnectState = DJIDrone.getDjiCamera().getCameraConnectIsOk();
                    if(bConnectState){
                        mConnectStateTextView.setText(R.string.camera_connection_ok);
                    }
                    else{
                        mConnectStateTextView.setText(R.string.camera_connection_break);
                    }
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                fmController.startFollowMe();
                break;
            case R.id.btnEnd:
                fmController.endFollowMe();
                break;
            case R.id.btnIncreaseAlt:
                fmController.increaseAltitude();
                break;
            case R.id.btnDecreaseAlt:
                fmController.decreaseAltitude();
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        boolean move = event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE;

        if (move) {
            switch (v.getId()) {
                case R.id.btnForward:
                    joystickController.setPitch(JoystickControllerImpl.MAX_SPEED);
                    break;
                case R.id.btnBackward:
                    joystickController.setPitch(-JoystickControllerImpl.MAX_SPEED);
                    break;
                case R.id.btnLeft:
                    joystickController.setRoll(-JoystickControllerImpl.MAX_SPEED);
                    break;
                case R.id.btnRight:
                    joystickController.setRoll(JoystickControllerImpl.MAX_SPEED);
                    break;
            }
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_ENABLE_BT:
                switch (resultCode) {
                    case RESULT_OK:
                        Log.d(TAG, "onActivityResult: resultCode: RESULT_OK");
                        //btController.startBTServer();
                        btServer.start();
                        break;
                    case RESULT_CANCELED:
                        Log.d(TAG, "onActivityResult: resultCode: RESULT_CANCEL");
                        break;
                    default:
                        Log.d(TAG, "onActivityResult: resultCode: UNKNOWN: " + resultCode);
                        break;
                }
                break;
            default:
                Log.d(TAG, "onActivityResult: unkown request code: " + requestCode);
                break;
        }
    }

    class Task extends TimerTask {
        //int times = 1;

        @Override
        public void run()
        {
            //Log.d(TAG ,"==========>Task Run In!");
            checkConnectState();
        }
    };
}
