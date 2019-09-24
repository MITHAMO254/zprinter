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
    private String TAG = MainActivity.class.getSimpleName();
    Button connect, print, server;
    TextView connect_status;
    EditText text;
    BluetoothAdapter adp;
    BluetoothDevice zprinter;
    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.d(TAG, "onReceive: "+ device.getName());
                String deviceName = device.getName();
                if(deviceName.equals("zprinter")){
                    zprinter = device;
                }

            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if(adp.isEnabled()){
            adp.disable();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connect =findViewById(R.id.connect);
        print =findViewById(R.id.print);
        server =findViewById(R.id.server);
        connect_status =findViewById(R.id.connection_status);
        text =findViewById(R.id.text);
        adp =BluetoothAdapter.getDefaultAdapter();

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status();
            }
        });

        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printMessage();
            }
        });

    }

    private void printMessage() {
        StringBuilder message = new StringBuilder();
        message.append(text.getText().toString()).append("\n");
        if (message.equals("")){
            Toast.makeText(this, "text cannot be empty", Toast.LENGTH_LONG).show();
        }else {

            byte[] bytes_to_print = message.toString().getBytes();
            BluetoothService service = new BluetoothService(zprinter);
            service.Out(bytes_to_print);
        }
    }

    public void status(){

        if (adp == null) {
            // Device doesn't support Bluetooth
            this.finish();
            Log.d(TAG, "status: device dosent support bluetooth");
        }
        if (adp.isEnabled()){
            Log.d(TAG, "status: Bluetooth is enabled");
            connect_status.setText("Bluetooth is on");

            connect_status.setTextColor(getResources().getColor(R.color.colorGreen));
            findprinter();

        }
        if (!adp.isEnabled()){
            Log.d(TAG, "status: bluetooth is off");
            connect_status.setText("Bluetooth is off");
            connect_status.setTextColor(getResources().getColor(R.color.colorOrange));
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,1);
        }

        }

    public void findprinter(){
        Set<BluetoothDevice> pairedDevices = adp.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                zprinter = device;
                String deviceName = device.getName();
                if (deviceName.equals("zprinter")){
                    Log.d(TAG, "findprinter: "+ deviceName);
                    connect_status.setText("Printer found, connecting...");
                    break;
                }
                scan_devices();
            }

        }

    }

    private void scan_devices() {
        adp.startDiscovery();
        //create an intent filter
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //register broadcast receiver
        registerReceiver(receiver, filter);



    }
  @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==1){
            if(resultCode == RESULT_OK){
                findprinter();
            }else {
                this.finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}


