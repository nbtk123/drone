package com.dji.sdkdemo.BrainControl;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dji.sdkdemo.R;

import java.util.Timer;
import java.util.TimerTask;

import dji.sdk.api.DJIDrone;
import dji.sdk.api.GroundStation.DJIGroundStationMissionPushInfo;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef;
import dji.sdk.api.GroundStation.DJIHotPointInitializationInfo;
import dji.sdk.api.MainController.DJIMainControllerSystemState;
import dji.sdk.interfaces.DJIGroundStationCancelCallBack;
import dji.sdk.interfaces.DJIGroundStationExecuteCallBack;
import dji.sdk.interfaces.DJIGroundStationMissionPushInfoCallBack;
import dji.sdk.interfaces.DJIGroundStationTakeOffCallBack;
import dji.sdk.interfaces.DJIMcuUpdateStateCallBack;
import dji.sdk.interfaces.DJIReceivedVideoDataCallBack;
import dji.sdk.widget.DjiGLSurfaceView;

public class BrainControlActivity extends Activity implements View.OnClickListener {

    protected Button btnStart;
    protected Button btnEnd;
    protected Button btnIncreaseAlt;
    protected Button btnDecreaseAlt;
    protected DjiGLSurfaceView mDjiGLSurfaceView;
    protected TextView mHotPointTextView;
    protected TextView mConnectStateTextView;

    FollowMeController fmController;

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

        fmController = new FollowMeController(this);

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
        if(DJIDrone.getDjiCamera() != null)
            DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(null);
        mDjiGLSurfaceView.destroy();
        fmController.stopLocationUpdates();
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
