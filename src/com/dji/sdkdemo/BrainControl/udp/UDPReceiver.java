package com.dji.sdkdemo.BrainControl.udp;

import com.dji.sdkdemo.BrainControl.JoystickController;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by nbtk123 on 10/03/2017.
 */

public class UDPReceiver extends Thread {

    private DroneOrderParser parser;
    private JoystickController joystickController;

    public UDPReceiver(DroneOrderParser parser, JoystickController joystickController) {
        this.parser = parser;
        this.joystickController = joystickController;
    }

    @Override
    public void run() {
        try {
            DatagramSocket serverSocket = new DatagramSocket(9876);
            byte[] receiveData = new byte[1024];

            while(true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                parser.parse(receivePacket, new DroneOrderParser.ParserListener() {

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
                });
            }
        } catch (Exception e) {

        }
    }
}
