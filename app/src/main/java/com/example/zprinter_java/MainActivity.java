package com.example.zprinter_java;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    //
    // tag to use in the logacat.
    private String TAG = MainActivity.class.getSimpleName();
    //
    //declaring Global variables;
    Button connect, print, server;
    TextView connect_status;
    EditText text;
    //
    //declaring a Bluetooth adapter;
    BluetoothAdapter adp;
    //define a BluetoothDevice;
    BluetoothDevice zprinter;
    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        //
        //broadcaster receiver accepts a context and an Intent
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //
                //log to check for devices found after scanning.

                Log.d(TAG, "onReceive: "+ device.getName());
                //
                //put devices found in variable DeviceName.
                String deviceName = device.getName();
                //
                //check for Zprinter in devices found.
                if(deviceName.equals("zprinter")){
                    //
                    //set device to Zprinter.
                    zprinter = device;

                }else{
                    Toast.makeText(MainActivity.this,"printer not found",Toast.LENGTH_LONG).show();

                }

            }
        }
    };
    //
    //ovverride the ondestroy method to stop discovering devices.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //
        //unregister the receiver
        unregisterReceiver(receiver);
        if(adp.isEnabled()){
            //disable the adapter at this point.
            adp.disable();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        //set the layout to view on app launch.
        setContentView(R.layout.activity_main);
        //
        //find all XML views in our layout by their IDs.
        connect =findViewById(R.id.connect);
        print =findViewById(R.id.print);
        server =findViewById(R.id.server);
        connect_status =findViewById(R.id.connection_status);
        text =findViewById(R.id.text);
        //
        //defining the BluetoothAdapter
        adp =BluetoothAdapter.getDefaultAdapter();
        //
        //assign an onclicklistener to our connect button.
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //call the status method that gives us the state of bluetooth
                //i.e either on or off
                status();
            }
        });
        //
        //assign an onclicklistener to our print button.
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                //on clicking the print button, call printMessage function.
                printMessage();
            }
        });

    }

    private void printMessage() {
        //
        //StringBuilder helps us display multiline text.
        StringBuilder message = new StringBuilder();

        message.append(text.getText().toString()).append("\n");
        //
        //if the edittext is empty, toast a message to show the user that the edittext can`t be empty.
        //i.e it must have some data to print.
        if (message.equals(" ")){
            Toast.makeText(this, "text cannot be empty", Toast.LENGTH_LONG).show();
        }else {
            //
            //transform the message data to print, into an array of bytes
            byte[] bytes_to_print = message.toString().getBytes();
            //
            //instantiate class BluetoothService which accepts a device
            //in this case....(zprinter).
            BluetoothService service = new BluetoothService(zprinter);
            //
            //create a public method to access private method printing() in class BluetoothService.
            service.Out(bytes_to_print);
        }
    }
    //
    // a method to check bluetooth status(on,off,is_supported)
    public void status(){
        // Device doesn't support Bluetooth
        if (adp == null) {
            //
            //if bluetooth is not supported, kill the activity.
            this.finish();
            Log.d(TAG, "status: device dosent support bluetooth");
        }
        //
        //if bluetooth is enabled, display the connection status  and call method findprinter()
        if (adp.isEnabled()){
            Log.d(TAG, "status: Bluetooth is enabled");
            connect_status.setText("Bluetooth is on");

            connect_status.setTextColor(getResources().getColor(R.color.colorGreen));
            findprinter();

        }
        //
        //if bluetooth is off, create an intent to enable bluetooth.
        if (!adp.isEnabled()){
            Log.d(TAG, "status: bluetooth is off");
            connect_status.setText("Bluetooth is off");
            connect_status.setTextColor(getResources().getColor(R.color.colorOrange));
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,1);
        }

        }

    public void findprinter(){
        //
        //call our adapter and get bonded devices and put them in a set called paired devices
        Set<BluetoothDevice> pairedDevices = adp.getBondedDevices();
        //
        // if there are paired devices. Get the name of each paired device.
        if (pairedDevices.size() > 0) {

            for (BluetoothDevice device : pairedDevices) {
                //checking for zprinter
                zprinter = device;
                //
                // get the names of paired devices.
                String deviceName = device.getName();
                if (deviceName.equals("zprinter")){
                    Log.d(TAG, "findprinter: "+ deviceName);
                    //
                    //inform the user that the printer has been found and its connecting.
                    connect_status.setText("Printer found, connecting...");
                    break;
                }
                //
                //else, call method scan_devices to check for available devices.
                scan_devices();
            }

        }

    }

    private void scan_devices() {
        //
        //call the adapter to start discovery.
        adp.startDiscovery();
        //
        //create an intent filter
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //
        //register broadcast receiver
        registerReceiver(receiver, filter);



    }
    //
    //override method onActivityResult
  @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //
        //give a specific I.D to the request code.
        if(requestCode==1){
            if(resultCode == RESULT_OK){
                //
                //call method findprinter()
                findprinter();
            }else {
                //
                //else kill the activity.
                this.finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}


