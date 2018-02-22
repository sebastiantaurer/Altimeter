package com.example.taurerstand.altimeter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    // EXTRA strings
    public static String EXTRA_DEVICE_NAME = "device_name";
    public static String EXTRA_DEVICE_ADDRESS = "device_address";


    // Member fields
    private BluetoothAdapter btAdapter;
    private ArrayAdapter<String> pairedDevicesArrayAdapter;

    // textview for connection status
    ListView pairedListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize array adapter for paired devices
        pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.listview_item_bluetooth_device);

        // Find and set up the ListView for paired devices
        pairedListView = (ListView) findViewById(R.id.listView);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(btDeviceClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshBluetoothDeviceList();
    }

    private AdapterView.OnItemClickListener btDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            String info[] = ((TextView) view).getText().toString().split("\n");
            String name = info[0];
            String address = info[1];

            Intent intent = new Intent(MainActivity.this, AltimeterActivity.class);
            intent.putExtra(EXTRA_DEVICE_NAME, name);
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
            startActivity(intent);
        }
    };

    public void btRefreshPressed(View view){
        refreshBluetoothDeviceList();
    }

    private void refreshBluetoothDeviceList(){
        //It is best to check BT status at onResume in case something has changed while app was paused etc
        checkBTState();

        pairedDevicesArrayAdapter.clear();// clears the array so items aren't duplicated when resuming from onPause

        // Get the local Bluetooth adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices and append to pairedDevices list
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        // Add previously paired devices to the array
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            pairedDevicesArrayAdapter.add("no devices paired");
        }
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
