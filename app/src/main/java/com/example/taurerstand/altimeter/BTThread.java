package com.example.taurerstand.altimeter;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by Taurer on 04.02.2018.
 */

public class BTThread extends Thread {

    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private BlockingQueueFIFO baroValues;
    private Handler btHandler;

    //creation of the connect thread
    public BTThread(BluetoothSocket socket, Handler btHandler) {
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        baroValues = new BlockingQueueFIFO(11);
        this.btHandler = btHandler;

        try {
            //Create I/O streams for connection
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[256];
        int bytes;
        String receivedMessage;
        String[] partsOfReceivedMessage;
        String[] sensorValue;

        // Keep looping to listen for received messages
        while (true) {
            try {
                bytes = mmInStream.read(buffer);            //read bytes from input buffer
                receivedMessage = new String(buffer, 0, bytes);

                partsOfReceivedMessage = receivedMessage.split("#");

                for (int i = 1; i < partsOfReceivedMessage.length; i++) {
                    sensorValue = partsOfReceivedMessage[i].split(":");
                    if(sensorValue.length == 2){
                        if((sensorValue[1].length() > 0) && sensorValue[0].startsWith("$baro") && (sensorValue[1].length() > 0) && sensorValue[1].endsWith("$")){
                            baroValues.add(Integer.parseInt(sensorValue[1].substring(0, sensorValue[1].length()-1)));
                        }else if(sensorValue[0].equals("temp")){
                            //ToDo implement temperature
                        }
                    }
                }


                btHandler.obtainMessage(0, baroValues.getAVG()).sendToTarget();
                btHandler.obtainMessage(1, baroValues.getMedian()).sendToTarget();

            } catch (IOException e) {
                break;
            }

            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    //write method
    public void write(String input) throws IOException {
        mmOutStream.write(input.getBytes());
    }
}