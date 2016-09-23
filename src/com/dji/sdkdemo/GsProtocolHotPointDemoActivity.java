/**   
 * Description  : TODO
 * @Title       : GsProtocolHotPointDemoActivity.java 
 * @Package     : com.dji.sdkdemo 
 * @author 	    : changjian.xu  
 * @date        : 2015年3月28日 下午3:39:25 
 * @version     : 1.0
 */


package com.dji.sdkdemo;

import java.util.Timer;
import java.util.TimerTask;

import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIError;
import dji.sdk.api.DJIDroneTypeDef.DJIDroneType;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationCancelResult;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationHotPointInterestDirection;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationHotPointNavigationMode;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationHotPointSurroundDirection;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationHoverResult;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationResult;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationTakeOffResult;
import dji.sdk.api.GroundStation.DJIGroundStationMissionPushInfo;
import dji.sdk.api.GroundStation.DJIHotPointInitializationInfo;
import dji.sdk.api.MainController.DJIMainControllerSystemState;
import dji.sdk.interfaces.DJIExecuteResultCallback;
import dji.sdk.interfaces.DJIGroundStationCancelCallBack;
import dji.sdk.interfaces.DJIGroundStationExecuteCallBack;
import dji.sdk.interfaces.DJIGroundStationHoverCallBack;
import dji.sdk.interfaces.DJIGroundStationMissionPushInfoCallBack;
import dji.sdk.interfaces.DJIGroundStationTakeOffCallBack;
import dji.sdk.interfaces.DJIMcuUpdateStateCallBack;
import dji.sdk.interfaces.DJIReceivedVideoDataCallBack;
import dji.sdk.widget.DjiGLSurfaceView;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/** 
 * <h3>Description	: TODO</h3>
 * @author      : changjian.xu
 * @date        : 2015年3月28日 下午3:39:25 
 * @version     : V1.0
 */

public class GsProtocolHotPointDemoActivity extends DemoBaseActivity implements OnClickListener
{
    private static final String TAG = "GsProtocolFollowMeDemoActivity";
    private static final int NAVI_MODE_HOT_POINT = 2;
    private static final int NAVI_MODE_ATTITUDE = 0;
    
    protected static final int SHOWTOAST = 1;
    
    private Button mOpenGroundStation;
    private Button mStartHotPoint;
    private Button mPauseHotPoint;
    private Button mResumeHotPoint;
    private Button mCancelHotPoint;
    private Button mCloseGroundStation;
    
    private TextView mConnectStateTextView;
    private TextView mHotPointTextView;
    
    private Timer mTimer;

    private double latitude;
    private double longitude;

    private boolean getHomePiontFlag = false;
    
    private DjiGLSurfaceView mDjiGLSurfaceView;
    private DJIReceivedVideoDataCallBack mReceivedVideoDataCallBack = null;
    
    private String HpInfoString = "";
    
    DJIGroundStationMissionPushInfoCallBack mGroundStationMissionPushInfoCallBack;

    class Task extends TimerTask {
        //int times = 1;

        @Override
        public void run() 
        {
            //Log.d(TAG ,"==========>Task Run In!");
            checkConnectState(); 
        }

    };
    
    private void checkConnectState(){
        
        GsProtocolHotPointDemoActivity.this.runOnUiThread(new Runnable(){

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
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_gs_protocol_hotpoint_demo);
        
        mDjiGLSurfaceView = (DjiGLSurfaceView)findViewById(R.id.DjiSurfaceView_gs);
        
        mOpenGroundStation = (Button)findViewById(R.id.OpenGsButton);
        mStartHotPoint = (Button)findViewById(R.id.StartHotPointBtn);
        mPauseHotPoint = (Button)findViewById(R.id.PauseHotPoint);
        mResumeHotPoint = (Button)findViewById(R.id.ResumeHotPoint);
        mCancelHotPoint = (Button)findViewById(R.id.CancelHotPoint);
        mCloseGroundStation = (Button)findViewById(R.id.CloseGroundStation);
        
        mHotPointTextView = (TextView) findViewById(R.id.HotPointInfoTV);
        
        mConnectStateTextView = (TextView)findViewById(R.id.ConnectStateGsTextView);
        
        mOpenGroundStation.setOnClickListener(this);
        mStartHotPoint.setOnClickListener(this);
        mPauseHotPoint.setOnClickListener(this);
        mResumeHotPoint.setOnClickListener(this);
        mCancelHotPoint.setOnClickListener(this);
        mCloseGroundStation.setOnClickListener(this);
        
        mDjiGLSurfaceView.start();
        
        mReceivedVideoDataCallBack = new DJIReceivedVideoDataCallBack(){

            @Override
            public void onResult(byte[] videoBuffer, int size)
            {
                // TODO Auto-generated method stub
                mDjiGLSurfaceView.setDataToDecoder(videoBuffer, size);
            }

            
        };
        
        DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(mReceivedVideoDataCallBack);
        
        DJIDrone.getDjiMC().setMcuUpdateStateCallBack(new DJIMcuUpdateStateCallBack() {

            @Override
            public void onResult(DJIMainControllerSystemState state)
            {
                // TODO Auto-generated method stub
                latitude = state.homeLocationLatitude;
                longitude = state.homeLocationLongitude;
                
                if(latitude != -1 && longitude != -1 && latitude != 0 && longitude != 0){
                    getHomePiontFlag = true;
                }
                else{
                    getHomePiontFlag = false;
                }

            }
            
        });
        
        mGroundStationMissionPushInfoCallBack = new DJIGroundStationMissionPushInfoCallBack() {

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
                
                GsProtocolHotPointDemoActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        mHotPointTextView.setText(HpInfoString);
                    }
                    
                });
            }
            
        };
        
        DJIDrone.getDjiGroundStation().setGroundStationMissionPushInfoCallBack(mGroundStationMissionPushInfoCallBack);
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        
        mTimer = new Timer();
        Task task = new Task();
        mTimer.schedule(task, 0, 500);
        
        DJIDrone.getDjiMC().startUpdateTimer(1000);
        
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        if(mTimer!=null) {            
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
        
        super.onPause();
        DJIDrone.getDjiMC().stopUpdateTimer();
    }
    
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }
    
    @Override
    protected void onDestroy()
    {
        // TODO Auto-generated method stub
        
        
        if(DJIDrone.getDjiCamera() != null)
        DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(null);
        mDjiGLSurfaceView.destroy();
        super.onDestroy();
    }
    
    private Handler handler = new Handler(new Handler.Callback() {
        
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SHOWTOAST:
                    setResultToToast((String)msg.obj); 
                    break;
                    
                default:
                    break;
            }
            return false;
        }
    });
    
    public void onReturn(View view){
        Log.d(TAG ,"onReturn");  
        this.finish();
    }
    
    private void setResultToToast(String result){
        Toast.makeText(GsProtocolHotPointDemoActivity.this, result, Toast.LENGTH_SHORT).show();
    }
    
    private boolean checkGetHomePoint(){
        if(!getHomePiontFlag){
            setResultToToast(getString(R.string.gs_not_get_home_point));
        }
        return getHomePiontFlag;
    }
    /** 
     * @param v 
     * @see android.view.View.OnClickListener#onClick(android.view.View) 
     */ 	
    @Override
    public void onClick(View v)
    {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.OpenGsButton : {
                
                if(!checkGetHomePoint()) return;
                
                DJIDrone.getDjiGroundStation().openGroundStation(new DJIGroundStationExecuteCallBack() {

                    @Override
                    public void onResult(GroundStationResult result)
                    {
                        // TODO Auto-generated method stub
                        String ResultsString = "return code =" + result.name();
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                    }
                    
                });
                
                break;
            }
            
            case R.id.StartHotPointBtn : {
                DJIHotPointInitializationInfo info = new DJIHotPointInitializationInfo();
                info.latitude = this.latitude;
                info.longitude = this.longitude;
                info.altitude = 30;
                info.radius = 5;
                info.velocity = 5;
                info.surroundDirection = GroundStationHotPointSurroundDirection.Anit_Clockwise;
                info.interestDirection = GroundStationHotPointInterestDirection.South;
                info.navigationMode = GroundStationHotPointNavigationMode.Backward_To_Hot_Point;
                DJIDrone.getDjiGroundStation().startHotPoint(info, new DJIGroundStationTakeOffCallBack() {
                    @Override
                    public void onResult(GroundStationTakeOffResult result)
                    {
                        // TODO Auto-generated method stub
                        String ResultsString = "return code =" + result.name();
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                    }
                });
                break;
            }
            
            case R.id.PauseHotPoint : {
                DJIDrone.getDjiGroundStation().pauseHotPoint(new DJIGroundStationHoverCallBack() {

                    @Override
                    public void onResult(GroundStationHoverResult result)
                    {
                        // TODO Auto-generated method stub
                        String ResultsString = "return code =" + result.name();
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                    }
                    
                });
                break;
            }
            
            case R.id.ResumeHotPoint : {
                DJIDrone.getDjiGroundStation().resumeHotPoint(new DJIGroundStationHoverCallBack() {

                    @Override
                    public void onResult(GroundStationHoverResult result)
                    {
                        // TODO Auto-generated method stub
                        String ResultsString = "return code =" + result.name();
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                    }
                    
                });
                break;
            }
            
            case R.id.CancelHotPoint : {
                DJIDrone.getDjiGroundStation().cancelHotPoint(new DJIGroundStationCancelCallBack() {

                    @Override
                    public void onResult(GroundStationCancelResult result)
                    {
                        // TODO Auto-generated method stub
                        String ResultsString = "return code =" + result.name();
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                    }
                    
                });
                break;
            }
            
            case R.id.CloseGroundStation : {

                DJIDrone.getDjiGroundStation().closeGroundStation(new DJIGroundStationExecuteCallBack() {

                    @Override
                    public void onResult(GroundStationResult result)
                    {
                        // TODO Auto-generated method stub
                        String ResultsString = "return code =" + result.name();
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                    }
                    
                });
                break;
            }
            
            default : {
                break;
            }
        }
    }

}
