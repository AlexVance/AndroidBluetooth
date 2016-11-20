package com.example.alexvance17.bluetooth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {

    //scan and connect
    Button scan;
    Button pair;
    ArrayList<BluetoothDevice> devicesArray;

    //Scan progress dialogue
    private ProgressDialog progressDlg;

    //Bluetooth Adapter
    Switch sw;
    BluetoothAdapter adapter;
    int BLUETOOTH_REQUEST_ON = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scan = (Button) findViewById(R.id.scanButton);
        pair = (Button) findViewById(R.id.pairButton);
        sw = (Switch) findViewById(R.id.bluetoothSwitch);

        adapter = BluetoothAdapter.getDefaultAdapter();

        progressDlg = new ProgressDialog(this);
        progressDlg.setMessage("Scanning...");
        progressDlg.setCancelable(false);
        progressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                adapter.cancelDiscovery();
            }

        });
        sw.setOnClickListener(new View.OnClickListener() {
                                  @Override
                                  public void onClick(View v) {
                                      adapter = BluetoothAdapter.getDefaultAdapter();

                                      //check bluetooth support
                                      if (adapter == null)
                                          Toast.makeText(getBaseContext(), "No bluetooth adapter found.", Toast.LENGTH_SHORT).show();
                                      else {
                                          if (!adapter.isEnabled()) {
                                              Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                              startActivityForResult(i, BLUETOOTH_REQUEST_ON);
                                          } else
                                              adapter.disable();
                                      }
                                  }
                              }
        );

        //scan for devices
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.startDiscovery();
            }
        });

        pair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter = BluetoothAdapter.getDefaultAdapter();

                if (adapter == null)
                    Toast.makeText(getBaseContext(), "No bluetooth adapter found.", Toast.LENGTH_SHORT).show();
                else {

                }
            }
        });

        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver, filter);
    }



    public void onActivityResult(int request_code, int result_code, Intent data){
        if(request_code == BLUETOOTH_REQUEST_ON){
            if(result_code == RESULT_OK)
                Toast.makeText(getBaseContext(), "Bluetooth successfully turned on.", Toast.LENGTH_SHORT).show();
            if(result_code == RESULT_CANCELED)
                Toast.makeText(getBaseContext(), "Bluetooth attempt failed.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
            return true;

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {

        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                devicesArray = new ArrayList<BluetoothDevice>();
                    progressDlg.show();

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progressDlg.dismiss();

                Intent newIntent = new Intent(MainActivity.this, DevicesActivity.class);

                newIntent.putParcelableArrayListExtra("device.list", devicesArray);

                startActivity(newIntent);

            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                devicesArray.add(device);

                Toast.makeText(getBaseContext(), "Found device " + device.getName(), Toast.LENGTH_SHORT).show();
            }
        }
    };
}
