package com.example.taurerstand.altimeter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class AltimeterActivity extends AppCompatActivity {

    private TextView tvAvg;
    private TextView tvMedian;
    private TextView tvAltitude;
    private TextView tvReferenceAltitude;

    private Handler btHandler;
    private String btDeviceName;

    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;
    private BTThread btThread;

    private double referenceAltitude = 0.0;
    private double sensorAltitude = 0.0;

    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_altimeter);

        tvAvg = findViewById(R.id.tvAVG);
        tvMedian = findViewById(R.id.tvMedian);
        tvAltitude = findViewById(R.id.tvAltitude);
        tvReferenceAltitude = findViewById(R.id.tvReferenceAltitude);


        btHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    sensorAltitude = (Double) msg.obj;
                    tvAvg.setText(String.format("%.2f", (referenceAltitude - sensorAltitude)));
                }
                if(msg.what == 1) {
                    sensorAltitude = (Double) msg.obj;
                    tvMedian.setText(String.format("%.2f", (referenceAltitude - sensorAltitude)));
                }
            }
        };

        checkBTState();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        String address = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);
        btDeviceName = intent.getStringExtra(MainActivity.EXTRA_DEVICE_NAME);


        BluetoothDevice btDevice = btAdapter.getRemoteDevice(address);
        try {
            btSocket = btDevice.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed!", Toast.LENGTH_LONG).show();
        }
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e1) {
                Toast.makeText(getBaseContext(), "Could not connect to BT socket!", Toast.LENGTH_LONG).show();
                finish();
            }
        }

        btThread = new BTThread(btSocket, btHandler);
        btThread.start();

        try {
            btThread.write("start");
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Could not connect!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            btSocket.close();
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Could not close BT socket!", Toast.LENGTH_LONG).show();
        }
    }

    public void btReferencePressed(View view) {
        Log.d(this.getPackageName(), "buttonPressed");
        referenceAltitude = sensorAltitude;
        tvReferenceAltitude.setText(String.format("%.2f", referenceAltitude));
    }

    //method to check if the device has Bluetooth and if it is on.
    //Prompts the user to turn it on if it is off
    private void checkBTState() {
        // Check device has Bluetooth and that it is turned on
        btAdapter = BluetoothAdapter.getDefaultAdapter(); // CHECK THIS OUT THAT IT WORKS!!!
        if (btAdapter == null) {
            Toast.makeText(getBaseContext(), "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!btAdapter.isEnabled()) {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }
}
