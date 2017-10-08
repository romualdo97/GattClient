package com.romualdo.ble.gattclient;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//import com.romualdo.ble.common.Ints;

//import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
                    implements TimePickerFragment.OnDataFromTimePickerFragment{

    private boolean SHOW_TOAST = true;

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String EXTRA__IS_ALARM_SETTED = "com.romualdo.ble.blink.isAlarmSetted";
    public static final String EXTRA__ALARM_VALUE_SETTED = "com.romualdo.ble.blink.alarmValue";

    //public static final String EXTRA_INPUTVAL = "com.romualdo.ble.blink.extra_inputval";

    public static final String MAC_ADDRESS = "CA:A5:4F:3A:A9:5C";
    public static final UUID UUID_SERVICE = UUID.fromString("0000fe84-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHARACTERISTIC_BUTTONSTATUS = UUID.fromString("2d30c082-f39f-4ce6-923f-3484ea480596");
    public static final UUID UUID_CHARACTERISTIC_LED = UUID.fromString("2d30c083-f39f-4ce6-923f-3484ea480596");

    /**
     * Services, characteristics, and descriptors are collectively
     * referred to as attributes and identified by UUIDs (128 bit number).
     * Of those 128 bits, you typically only care about the 16 bits highlighted
     * below. These digits are predefined by the Bluetooth Special Interest Group
     * (SIG).

     xxxxXXXX-xxxx-xxxx-xxxx-xxxxxxxxxxxx
     */

    /*
    Read and write descriptors for a particular characteristic.
    One of the most common descriptors used is the Client Characteristic
    Configuration Descriptor. This allows the client to set the notifications
    to indicate or notify for a particular characteristic. If the client sets
    the Notification Enabled bit, the server sends a value to the client whenever
    the information becomes available. Similarly, setting the Indications Enabled
    bit will also enable the server to send notifications when data is available,
    but the indicate mode also requires a response from the client.

    Source: https://goo.gl/EaK6au

     */

    // This is one of the most used descriptor: Client Characteristic Configuration Descriptor. 0x2902
    public static final UUID UUID_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_SET_ALARM = 2;

    private AlarmManager mAlarmManager;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private Button connectBtn;
    private Button disconectBtn;
    private TextView statusBtn;
    private TextView clockTxt;
    private Button btnOnOff;
    private boolean ledStatus = false;
    private boolean isLedServiceDiscovered = false;
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        private final String TAG = "mGattCallback";

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            super.onConnectionStateChange(gatt, status, newState);
            Log.i(TAG, status + " " + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectBtn.setEnabled(false);
                        disconectBtn.setEnabled(true);
                    }
                });
                //mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectBtn.setEnabled(true);
                        disconectBtn.setEnabled(false);
                    }
                });
            }
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {


            if (status == gatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(UUID_SERVICE);
                if (service != null) {
                    Log.i(TAG, "Service connected");
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_CHARACTERISTIC_BUTTONSTATUS);
                    if (characteristic != null) {
                        Log.i(TAG, "Characteristic connected");
                        gatt.setCharacteristicNotification(characteristic, true);
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_DESCRIPTOR);
                        if (descriptor != null) {
                            // Los descriptors son muy importntes
                            // TODO: Continue studying about descriptors in BLE
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                            Log.i(TAG, "Descriptor sended");
                        }
                    }

                    BluetoothGattCharacteristic characteristicLed = service.getCharacteristic(UUID_CHARACTERISTIC_LED);
                    if (characteristicLed != null) {
                        // TODO: Resolver problemas con hilos y descubriendo servicios
                        //writeCharacteristic(true);
                        isLedServiceDiscovered = true;
                        Log.i(TAG, "LED CHARACTERISTIC CONNECTED: " + isLedServiceDiscovered);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnOnOff.setEnabled(true);
                                //writeCharacteristic(true);
                                Log.i(TAG, "THIS CODE WAS EXECUTED: " + isLedServiceDiscovered);
                            }
                        });

                        // https://stackoverflow.com/questions/11123621/running-code-in-main-thread-from-another-thread
                        // execute code in main loop
                        // is necesario postear con un pequeño delay el message de encender el led por motivos aun desconocidos
                        // para la fisica teorica del momento.
                        Handler mainHandler = new Handler(Looper.getMainLooper());
                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {
                                writeCharacteristic(true);
                            }
                        };
                        mainHandler.postDelayed(myRunnable, 1000); // post in delay

                    }
                }
            }
        }
/*
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }*/

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            readBtnStateCharacteristic(characteristic);
        }

        private void readBtnStateCharacteristic(BluetoothGattCharacteristic characteristic) {
            if (UUID_CHARACTERISTIC_BUTTONSTATUS.equals(characteristic.getUuid())) {
                byte[] data = characteristic.getValue();
                //int state = Ints.fromByteArray(data);
                Log.i(TAG, data[0] + "");
                if (data[0] == 1) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            statusBtn.setText("Button Down");
                        }
                    });
                } else if (data[0] == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            statusBtn.setText("Button Up");
                        }
                    });
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mContext = this;
        setContentView(R.layout.activity_main);

        connectBtn = (Button) findViewById(R.id.buttonConnect);
        disconectBtn = (Button) findViewById(R.id.buttonDisconnect);
        statusBtn = (TextView) findViewById(R.id.btnStatus);
        clockTxt = (TextView) findViewById(R.id.clockView);
        btnOnOff = (Button) findViewById(R.id.btnOnOff);
        btnOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //turnOnOffLed();
                writeCharacteristic(false);
            }
        });
        btnOnOff.setEnabled(false);

        // When the app is opened not show buttons
        connectBtn.setVisibility(View.INVISIBLE);
        disconectBtn.setVisibility(View.INVISIBLE);

        // Initializes Bluetooth adapter.
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        // get the alarm manager
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            connectBtn.setVisibility(View.VISIBLE);
            disconectBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        boolean isAlarmSetted = intent.getBooleanExtra(EXTRA__IS_ALARM_SETTED, false);

        if (isAlarmSetted) {
            Toast.makeText(this, "Time to rock!!!", Toast.LENGTH_SHORT).show();

            String data = intent.getStringExtra(EXTRA__ALARM_VALUE_SETTED);
            clockTxt.setText(data);

            // Initializes Bluetooth adapter.
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            //mBluetoothGatt.discoverServices();

            /*try {
                if (startClient(null)) {
                    mBluetoothGatt.discoverServices();
                    writeCharacteristic(true);
                    Toast.makeText(this, "Characteristic written", Toast.LENGTH_SHORT).show();
                }
            } catch (Throwable e) {
                Toast.makeText(this, "Error writing characteristic", Toast.LENGTH_SHORT).show();
                Log.w(TAG, e.toString());
            }*/

            if (startClient()) {
                Log.i(TAG, "LED CHARACTERISTIC CONNECTED BEFORE discoverServices(): " + isLedServiceDiscovered);
                mBluetoothGatt.discoverServices();
                Log.i(TAG, "LED CHARACTERISTIC CONNECTED AFTER discoverServices(): " + isLedServiceDiscovered);

                /*if (isLedServiceDiscovered) {
                    writeCharacteristic(true);
                    Toast.makeText(this, "Characteristic written", Toast.LENGTH_SHORT).show();
                    //btnOnOff.setEnabled(true);
                    //if (turnOnOffLed()) {
                    //    Toast.makeText(this, "Characteristic written", Toast.LENGTH_SHORT).show();
                    //}
                }*/
            }


            intent.putExtra(EXTRA__IS_ALARM_SETTED, false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopClient();
        ledStatus = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        // just UI topics
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Log.w(TAG, "Bluetooth enabled");
                if (SHOW_TOAST) { Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show(); }
                connectBtn.setVisibility(View.VISIBLE);
                disconectBtn.setVisibility(View.VISIBLE);
            }
            else {
                // Si no se pudo conectar
                connectBtn.setVisibility(View.INVISIBLE);
                disconectBtn.setVisibility(View.INVISIBLE);
                if (SHOW_TOAST) { Toast.makeText(this, "Bluetooth not enabled, closing app...", Toast.LENGTH_SHORT).show(); }
                // TODO: Close the app if bluetooth not enabled by user
            }
        }
    }

    private boolean turnOnOffLed() {
        ledStatus = !ledStatus;
        BluetoothGattService ledService = mBluetoothGatt.getService(UUID_SERVICE);
        if (ledService == null) {
            if (SHOW_TOAST) { Toast.makeText(this, "Could not Get led service", Toast.LENGTH_SHORT).show(); }
            return false;
        }
        BluetoothGattCharacteristic ledCharacteristic = ledService.getCharacteristic(UUID_CHARACTERISTIC_LED);
        if (ledCharacteristic == null) {
            if (SHOW_TOAST) { Toast.makeText(this, "Could not Get led characteristic", Toast.LENGTH_SHORT).show(); }
            return false;
        }

        byte[] val = new byte[1];

        if (ledStatus) {
            val[0] = (byte) 1;
            Log.i(TAG, "Led status ON");
        } else {
            val[0] = (byte) 0;
        }
        ledCharacteristic.setValue(val);
        mBluetoothGatt.writeCharacteristic(ledCharacteristic);
        return true;
    }

    private boolean writeCharacteristic(boolean data) {

        /*BluetoothGattCharacteristic ledCharacteristic = mBluetoothGatt
                .getService(UUID_SERVICE)
                .getCharacteristic(UUID_CHARACTERISTIC_LED);*/
        BluetoothGattService ledService = mBluetoothGatt.getService(UUID_SERVICE);
        if (ledService == null) {
            if (SHOW_TOAST) { Toast.makeText(this, "Could not Get led service", Toast.LENGTH_SHORT).show(); }
            return false;
        }
        BluetoothGattCharacteristic ledCharacteristic = ledService.getCharacteristic(UUID_CHARACTERISTIC_LED);
        if (ledCharacteristic == null) {
            if (SHOW_TOAST) { Toast.makeText(this, "Could not Get led characteristic", Toast.LENGTH_SHORT).show(); }
            return false;
        }

        byte[] val = new byte[1];

        if (data) {
            val[0] = (byte) 1;
            //Log.i(TAG, "Led status ON");
        } else {
            val[0] = (byte) 0;
        }
        //val[0] = (byte) 1;
        ledCharacteristic.setValue(val);
        mBluetoothGatt.writeCharacteristic(ledCharacteristic);
        if (SHOW_TOAST) { Toast.makeText(this, "Written in led service, val = " + val[0], Toast.LENGTH_SHORT).show(); }
        return true;
    }

    // This is called when event onClick is fired
    public void startClient(View view) {
        startClient();
    }

    // This is called when event onClick is fired
    public boolean startClient() {
        Intent intent = getIntent();
        boolean isAlarmSetted = intent.getBooleanExtra(EXTRA__IS_ALARM_SETTED, false);
        try {
            BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(MAC_ADDRESS);
            mBluetoothGatt = bluetoothDevice.connectGatt(this, false, mGattCallback);
            if (!isAlarmSetted) showTimePickerDialog();
            if (mBluetoothGatt == null) {
                Log.w(TAG, "Unable to create GATT client");
                if (SHOW_TOAST) { Toast.makeText(this, "Cant connect to " + MAC_ADDRESS, Toast.LENGTH_SHORT).show(); }
                return false;
            } else {
                if (SHOW_TOAST) { Toast.makeText(this, "Connected to " + MAC_ADDRESS, Toast.LENGTH_SHORT).show(); }
                return true;
            }
        }
        catch (Exception e) {
            Log.w(TAG, e.toString());
            if (SHOW_TOAST) { Toast.makeText(this, "Error connecting to " + MAC_ADDRESS, Toast.LENGTH_SHORT).show(); }
            return false;
        }

    }

    // Called when onClik event of disconect button_selector is fired
    public void disconnect(View view) {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
    }

    // Called when onDestroy event is fired
    // TODO: Call this in onDestroy event of current Activity
    public void stopClient() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }

        if (mBluetoothAdapter != null) {
            mBluetoothAdapter = null;
        }
    }

    public void showTimePickerDialog() {
        DialogFragment timeFragment = new TimePickerFragment();
        timeFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void OnDataFromTimePickerFragment(String data) {
        clockTxt.setText(data);
        Log.d(TAG, data);
        String s_h = data.substring(0, 2); // string for hour
        String s_m = data.substring(3, 5); // string for minute
        //clockTxt.setText(s_h + ":" + s_m);
        int i_h = Integer.parseInt(s_h);
        int i_m = Integer.parseInt(s_m);

        setAlarm(i_h, i_m, data);

    }

    private void setAlarm(int hour, int minute, String data) {
        Toast.makeText(this, "Alarm setted", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA__IS_ALARM_SETTED, true);
        intent.putExtra(EXTRA__ALARM_VALUE_SETTED, data);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_SET_ALARM, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(calendar.MINUTE, minute);

        //mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000*10, pendingIntent);
        mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

}
