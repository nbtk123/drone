package com.dji.sdkdemo.BrainControl.udp;

import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashMap;

/**
 * Created by nbtk123 on 10/03/2017.
 */

public class DroneOrderParser {

    private static final String PAYLOAD_PITCH = "pitch";
    private static final String PAYLOAD_YAW = "yaw";
    private static final String PAYLOAD_ROLL = "roll";
    private static final String PAYLOAD_THROTTLE = "throttle";

    private static final String ACTION_MOVE = "move";
    private static final String ACTION_TAKE_PICTURE = "takePicture";
    private static final String ACTION_START_VIDEO = "startVideo";
    private static final String ACTION_STOP_VIDEO = "stopVideo";

    private ParserListener mParserListener;

    public interface ParserListener {
        void onPitch(int value);
        void onRoll(int value);
        void onThrottle(int value);
        void onYaw(int value);
        void takePicture();
        void startVideo();
        void stopVideo();
    }

    public DroneOrderParser(ParserListener listener) {
        mParserListener = listener;
    }

    // {
    //  action: move,
    //  payload: {pitch: 500, yaw: 500}
    // }

    // {action: takePicture}
    // {action: startVideo}
    // {action: stopVideo}

    public void parse(String data) throws Exception {

        //Patch for the BeeSeeEye program since it cannot get long strings in c:\work\data... csv file.
        //data = data.replace("pitch1000","{action: move, payload: {pitch: 1000}}");
        //data = data.replace("pitch-1000","{action: move, payload: {pitch: -1000}}");

        //Patch for the BeeSeeEye program since it cannot get long strings in c:\work\data... csv file.
        data = data.replace("yaw50","{action: move, payload: {yaw: 50}}");
        data = data.replace("yaw-50","{action: move, payload: {yaw: -50}}");
        //data = data.replace("halt","{action: move, payload: {yaw: 0, pitch: 0, roll: 0, throttle: 0}}");

        JSONObject json = new JSONObject(data);

        String action = json.optString("action");
        String payloadStr = json.optString("payload");

        if (action != null) {
            if (ACTION_MOVE.equals(action) && payloadStr != null) {
                JSONObject payload = new JSONObject(payloadStr.toLowerCase());
                if (payload.has(PAYLOAD_PITCH)) {
                    mParserListener.onPitch(payload.optInt(PAYLOAD_PITCH, 0));
                }
                if (payload.has(PAYLOAD_ROLL)) {
                    mParserListener.onRoll(payload.optInt(PAYLOAD_ROLL, 0));
                }
                if (payload.has(PAYLOAD_THROTTLE)) {
                    mParserListener.onThrottle(payload.optInt(PAYLOAD_THROTTLE, 0));
                }
                if (payload.has(PAYLOAD_YAW)) {
                    mParserListener.onYaw(payload.optInt(PAYLOAD_YAW, 0));
                }
            } else if (ACTION_TAKE_PICTURE.equals(action)) {
                mParserListener.takePicture();
            } else if (ACTION_START_VIDEO.equals(action)) {
                mParserListener.startVideo();
            } else if (ACTION_STOP_VIDEO.equals(action)) {
                mParserListener.stopVideo();
            }
        }
    }

    @Deprecated
    public void parse(DatagramPacket packet, ParserListener listener) throws Exception {
        JSONObject json = new JSONObject(new String(packet.getData()));

        String action = json.optString("action");
        String payloadStr = json.optString("payload");

        if (action != null && payloadStr != null) {

            action = action.toLowerCase();
            JSONObject payload = new JSONObject(payloadStr.toLowerCase());

            if (ACTION_MOVE.equals(action)) {
                if (payload.has(PAYLOAD_PITCH)) {
                    listener.onPitch(payload.optInt(PAYLOAD_PITCH, 0));
                } else if (payload.has(PAYLOAD_ROLL)) {
                    listener.onRoll(payload.optInt(PAYLOAD_ROLL, 0));
                } else if (payload.has(PAYLOAD_THROTTLE)) {
                    listener.onThrottle(payload.optInt(PAYLOAD_THROTTLE, 0));
                } else if (payload.has(PAYLOAD_YAW)) {
                    listener.onYaw(payload.optInt(PAYLOAD_YAW, 0));
                }
            } else if (ACTION_TAKE_PICTURE.equals(action)) {
                listener.takePicture();
            } else if (ACTION_START_VIDEO.equals(action)) {
                listener.startVideo();
            } else if (ACTION_STOP_VIDEO.equals(action)) {
                listener.stopVideo();
            }
        }
    }
}
