package com.example.zprinter_java;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService {
    private String TAG = BluetoothService.class.getSimpleName();
    private BluetoothDevice device;
    Printing printing;
    public BluetoothService(BluetoothDevice device){
        Log.d(TAG, "BluetoothService: has started");
        this.device = device;
        Client client =new Client();
        client.run();
    }

   private class Client extends Thread{
        BluetoothSocket socket = null;
        public Client(){
            Log.d(TAG, "Client: client has started");
            UUID uuid = UUID.fromString("0001101-0000-1000-8000-00805F9B34FB");

            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.e(TAG, "Client: "+e.getMessage());
                e.printStackTrace();
            }
        }
       @Override
       public void run() {
           super.run();
           try {
               socket.connect();
           } catch (IOException e) {
               Log.e(TAG, "run: "+e.getMessage());
               e.printStackTrace();
           }
           manageConnection(socket);
       }
   }

    private void manageConnection(BluetoothSocket socket) {
        printing = new Printing(socket);

    }

    public void Out(byte[] text_to_print){
        printing.write(text_to_print);
    }
    private class Printing {
        OutputStream outputStream;
        public Printing(BluetoothSocket socket){
            Log.d(TAG, "Printing: has started");

            try {
                outputStream = socket.getOutputStream();

            } catch (IOException e) {
                Log.e(TAG, "Printing: "+e.getMessage());
                e.printStackTrace();
            }

        }

        public void write(byte[] text_to_print){
            try {
                String x = new String(text_to_print);
                Log.d(TAG, "write: "+x);
                outputStream.write(text_to_print);
            } catch (IOException e) {
                Log.e(TAG, "write: "+e.getMessage());
                e.printStackTrace();
            }
        }

   }

}
