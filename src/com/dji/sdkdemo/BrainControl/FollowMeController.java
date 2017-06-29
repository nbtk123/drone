package com.dji.sdkdemo.BrainControl;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.dji.sdkdemo.R;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIDroneTypeDef;
import dji.sdk.api.GroundStation.DJIGroundStationFlyingInfo;
import dji.sdk.api.GroundStation.DJIGroundStationTask;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef;
import dji.sdk.api.GroundStation.DJIGroundStationWaypoint;
import dji.sdk.interfaces.DJIGroundStationExecuteCallBack;
import dji.sdk.interfaces.DJIGroundStationFlyingInfoCallBack;
import dji.sdk.interfaces.DJIGroundStationHoverCallBack;
import dji.sdk.interfaces.DJIGroundStationOneKeyFlyCallBack;
import dji.sdk.interfaces.DJIGroundStationTakeOffCallBack;

/**
 * Created by nbtk123 on 02/06/2016.
 */
public class FollowMeController implements LocationListener {

    public static final String TAG_DRONE_GS = "DRONE_GS";

    protected static final int SHOWTOAST = 1;
    protected static final float MAX_ALTITUDE = 100;
    protected static final float MIN_ALTITUDE = 0;
    protected static final float ALTITUDE_HOP_SIZE = 10f;

    protected WeakReference<Context> context;
    protected LocationManager locationManager;
    protected DJIGroundStationTask mTask;

    protected double latitude;
    protected double longitude;
    protected float altitude = 30f;

    protected boolean getHomePiontFlag = false;
    protected boolean isGSOpen = false;

    Timer mCheckModeForPauseTimer;
    private DJIGroundStationTypeDef.GroundStationFlightMode mFlightMode;

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

    public FollowMeController(Context context) {
        this.context = new WeakReference<Context>(context);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mTask = new DJIGroundStationTask();
    }

    public void startLocationUpdate() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    public void stopLocationUpdates() {
        locationManager.removeUpdates(this);
    }

    protected void startFollowMe() {
        openGS();
    }

    protected void openGS() {
        //if(!checkGetHomePoint()) return;

//        DJIDrone.getDjiGroundStation().openGroundStation(new DJIGroundStationExecuteCallBack() {
//
//            @Override
//            public void onResult(DJIGroundStationTypeDef.GroundStationResult result)
//            {
//                // TODO Auto-generated method stub
//                String ResultsString = "return code =" + result.name();
//                handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
//                Log.d(TAG_DRONE_GS, "openGroundStation = " + result.name());
//
//                isGSOpen = true;
//                flyToGroundStationTaskWaypoint();
//            }
//
//        });

        oneKeyFly();
    }

    protected  void oneKeyFly() {
        Log.d(TAG_DRONE_GS, "oneKeyFly");

        DJIDrone.getDjiGroundStation().setGroundStationFlyingInfoCallBack(new DJIGroundStationFlyingInfoCallBack(){

            @Override
            public void onResult(DJIGroundStationFlyingInfo flyingInfo) {
                // TODO Auto-generated method stub
                //Log.e(TAG, "DJIGroundStationFlyingInfo homeLocationLatitude " +flyingInfo.homeLocationLatitude);
                //Log.e(TAG, "DJIGroundStationFlyingInfo homeLocationLongitude " +flyingInfo.homeLocationLongitude);

                mFlightMode = flyingInfo.flightMode;
            }

        });

        DJIDrone.getDjiGroundStation().openGroundStation(new DJIGroundStationExecuteCallBack(){

            @Override
            public void onResult(DJIGroundStationTypeDef.GroundStationResult result) {
                // TODO Auto-generated method stub
                String ResultsString = "openGroundStation = " + result.name();
                handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));

                if(result == DJIGroundStationTypeDef.GroundStationResult.GS_Result_Successed){

                    //one key fly
                    DJIDrone.getDjiGroundStation().oneKeyFly(new DJIGroundStationOneKeyFlyCallBack(){
                        @Override
                        public void onResult(DJIGroundStationTypeDef.GroundStationOneKeyFlyResult result) {
                            // TODO Auto-generated method stub

                            String ResultsString = "oneKeyFly = " + result.name();
                            handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));

                            if(result == DJIGroundStationTypeDef.GroundStationOneKeyFlyResult.GS_One_Key_Fly_Successed){

                                if(DJIDrone.getDroneType() == DJIDroneTypeDef.DJIDroneType.DJIDrone_Vision){
                                    mCheckModeForPauseTimer = new Timer();
                                    CheckModeForPauseTask mCheckTask = new CheckModeForPauseTask();
                                    mCheckModeForPauseTimer.schedule(mCheckTask, 100, 1000);
                                }
                                else{
                                    //DealSuccessWithVibrate();
                                    Log.d(TAG_DRONE_GS, "oneKeyFly --> success!");
                                }
                            }
                            else{
                                //DealErrorWithVibrate();
                                Log.d(TAG_DRONE_GS, "oneKeyFly --> FAIL!");
                            }
                        }

                    });
                }
                else{
                    //DealErrorWithVibrate();
                    Log.d(TAG_DRONE_GS, "oneKeyFly --> openGroundStation --> FAIL!");
                }

            }

        });
    }

    protected void flyToGroundStationTaskWaypoint() {
        Log.d(TAG_DRONE_GS, "flyToGroundStationTaskWaypoint() : lat=" + latitude + ", lon=" + longitude + ", alt=" + altitude);

        DJIDrone.getDjiGroundStation().startGroundStationTask(new DJIGroundStationTakeOffCallBack() {
            @Override
            public void onResult(DJIGroundStationTypeDef.GroundStationTakeOffResult result) {
                // TODO Auto-generated method stub
                String ResultsString = "return code =" + result.name();
                handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));

                DJIDrone.getDjiGroundStation().pauseGroundStationTask(new DJIGroundStationHoverCallBack() {
                    @Override
                    public void onResult(DJIGroundStationTypeDef.GroundStationHoverResult groundStationHoverResult) {
                        Log.d(TAG_DRONE_GS, "pauseGroundStationTask = " + groundStationHoverResult.name());
                    }
                });
            }
        });
        /*DJIHotPointInitializationInfo info = new DJIHotPointInitializationInfo();
        info.latitude = latitude;
        info.longitude = longitude;
        info.altitude = altitude;
        info.radius = 1;
        info.velocity = 5;
        info.surroundDirection = DJIGroundStationTypeDef.GroundStationHotPointSurroundDirection.Anit_Clockwise;
        info.interestDirection = DJIGroundStationTypeDef.GroundStationHotPointInterestDirection.South;
        info.navigationMode = DJIGroundStationTypeDef.GroundStationHotPointNavigationMode.Backward_To_Hot_Point;
        DJIDrone.getDjiGroundStation().flyToGroundStationTaskWaypoint(info, new DJIGroundStationTakeOffCallBack() {

            @Override
            public void onResult(DJIGroundStationTypeDef.GroundStationTakeOffResult result)
            {
                // TODO Auto-generated method stub
                String ResultsString = "return code =" + result.name();
                handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                Log.d(TAG_DRONE_GS, "flyToGroundStationTaskWaypoint = " + result.name());
            }

        });*/
    }

    protected void updateGroundStationTask() {

        if (isGSOpen) {
            mTask.RemoveAllWaypoint();

            DJIGroundStationWaypoint androidDeviceWaypoint = new DJIGroundStationWaypoint(latitude, longitude);
            androidDeviceWaypoint.altitude = altitude;
            androidDeviceWaypoint.speed = 2; // slow 2
            androidDeviceWaypoint.heading = 360;
            androidDeviceWaypoint.maxReachTime = 0;
            androidDeviceWaypoint.stayTime = 30; //TODO: check what stayTime is?
            mTask.addWaypoint(androidDeviceWaypoint);

            DJIDrone.getDjiGroundStation().uploadGroundStationTask(mTask, new DJIGroundStationExecuteCallBack() {

                @Override
                public void onResult(DJIGroundStationTypeDef.GroundStationResult result) {
                    // TODO Auto-generated method stub
                    String ResultsString = "return code =" + result.name();
                    handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                    Log.d(TAG_DRONE_GS, "updateGroundStationTask : uploadGroundStationTask = " + result.name());

                    flyToGroundStationTaskWaypoint();
                }

            });
        }

        if (isGSOpen) {
            /*Log.d(TAG_DRONE_GS, "updateGroundStationTask()");
            DJIDrone.getDjiGroundStation().cancelHotPoint(new DJIGroundStationCancelCallBack() {

                @Override
                public void onResult(DJIGroundStationTypeDef.GroundStationCancelResult result)
                {
                    // TODO Auto-generated method stub
                    String ResultsString = "return code =" + result.name();
                    handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                    Log.d(TAG_DRONE_GS, "updateGroundStationTask : cancelHotPoint = " + result.name());
                    flyToGroundStationTaskWaypoint();
                }

            });
            flyToGroundStationTaskWaypoint();*/
        }
    }

    /*protected void cancelHotPoint() {
        DJIDrone.getDjiGroundStation().cancelHotPoint(new DJIGroundStationCancelCallBack() {

            @Override
            public void onResult(DJIGroundStationTypeDef.GroundStationCancelResult result)
            {
                // TODO Auto-generated method stub
                String ResultsString = "return code =" + result.name();
                handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                Log.d(TAG_DRONE_GS, "cancelHotPoint = " + result.name());
                closeGS();
            }

        });
    }*/

    protected void endFollowMe() {
        //cancelHotPoint();
        closeGS();
    }

    public void increaseAltitude() {
        if (altitude < MAX_ALTITUDE) {
            altitude += ALTITUDE_HOP_SIZE;
        }
    }
    public void decreaseAltitude() {
        if (altitude > MIN_ALTITUDE) {
            altitude -= ALTITUDE_HOP_SIZE;
        }
    }

    protected void closeGS() {
        isGSOpen = false;
        DJIDrone.getDjiGroundStation().closeGroundStation(new DJIGroundStationExecuteCallBack() {

            @Override
            public void onResult(DJIGroundStationTypeDef.GroundStationResult result)
            {
                // TODO Auto-generated method stub
                String ResultsString = "return code =" + result.name();
                handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
            }

        });
    }

    protected boolean checkGetHomePoint() {
        if(!getHomePiontFlag && context.get() != null){
            setResultToToast(context.get().getString(R.string.gs_not_get_home_point));
        }
        return getHomePiontFlag;
    }

    protected void setResultToToast(String result){
        if (context.get() != null) {
            Toast.makeText(context.get(), result, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isGetHomePiontFlag() {
        return getHomePiontFlag;
    }
    public void setGetHomePiontFlag(boolean getHomePiontFlag) {
        this.getHomePiontFlag = getHomePiontFlag;
    }

    // LocationListener implementation START
    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        if (location.getAccuracy() <= 10) {
            //updateGroundStationTask();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            startLocationUpdate();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            stopLocationUpdates();
        }
    }
    // LocationListener implementation END


    class CheckModeForPauseTask extends TimerTask {

        @Override
        public void run()
        {
            if(mFlightMode == DJIGroundStationTypeDef.GroundStationFlightMode.GS_Mode_Pause_1 || mFlightMode == DJIGroundStationTypeDef.GroundStationFlightMode.GS_Mode_Pause_2 || mFlightMode == DJIGroundStationTypeDef.GroundStationFlightMode.GS_Mode_Gps_Atti){
                if(checkGetHomePoint()){
                    DJIDrone.getDjiGroundStation().pauseGroundStationTask(new DJIGroundStationHoverCallBack(){

                        @Override
                        public void onResult(DJIGroundStationTypeDef.GroundStationHoverResult result) {
                            // TODO Auto-generated method stub
                            String ResultsString = "pause return code =" + result.name();
                            handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));

                            if(result == DJIGroundStationTypeDef.GroundStationHoverResult.GS_Hover_Successed){
                                if(mCheckModeForPauseTimer != null){
                                    mCheckModeForPauseTimer.cancel();
                                    mCheckModeForPauseTimer.purge();
                                    mCheckModeForPauseTimer = null;

                                    Log.d(TAG_DRONE_GS, "CheckModeForPauseTask --> pauseGroundStationTask --> success");
                                }
                            }
                        }
                    });
                }
                return;
            }


//        	if(mFlightMode == GroundStationFlightMode.GS_Mode_Waypoint){
//        		if(checkGetHomePoint()){
//                    DJIDrone.getDjiGroundStation().pauseGroundStationTask(new DJIGroundStationHoverCallBack(){
//
//                        @Override
//                        public void onResult(GroundStationHoverResult result) {
//                            // TODO Auto-generated method stub
//                            String ResultsString = "pause return code =" + result.name();
//                            handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
//
//                            if(result == GroundStationHoverResult.GS_Hover_Successed){
//                            	if(mCheckModeForPauseTimer != null){
//                	        		mCheckModeForPauseTimer.cancel();
//                		            mCheckModeForPauseTimer.purge();
//                		            mCheckModeForPauseTimer = null;
//
//                		            DealSuccessWithVibrate();
//                        		}
//                            }
//                        }
//                    });
//            	}
//
//        	}

            Log.d(TAG_DRONE_GS ,"mFlightMode==========>"+mFlightMode);
        }

    };
}
