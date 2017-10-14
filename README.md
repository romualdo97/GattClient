# IoT: ¿Cómo usar bluetooth low energy en Android®?

A continuación, desarrollaremos una pequeña aplicación móvil usando Android Studio® que se conectará con un dispositivo Simblee® usando bluetooth low energy, el dispositivo Simblee® será programado usando el entorno de desarrollo Arduino®.

La aplicación Android permitirá configurar una alarma silenciosa que consiste de un led que se enciende y apaga en un dispositivo simblee.

---

## ¿Cómo funciona?

![enter image description here](https://i.imgur.com/hGSkxfA.png)

Se escribe un programa con tres funcionalidades básicas: `setAlarm()`, `cancerlAlarm()` y `turnOffAlarm()` que usando la API de Android controlara el dispositivo bluetooth embebido en el hardware del celular para dar órdenes al dispositivo host simblee. A continuación, describiremos con más detalle cada una de las tres funcionalidades del dispositivo.

### `setAlarm()`

![enter image description here](https://i.imgur.com/QDF1ibU.png)

Cuando ejecutemos la función `setAlarm` la aplicación nos mostrara una ventana flotante (en Android se llama fragment) para poder seleccionar la hora en la que debe sonar la alarma.

### `cancelAlarm()`

![enter image description here](https://i.imgur.com/VKoAdJM.png)

Cuando se cancela la alarma el teléfono móvil se desconectará del dispositivo simblee, la aplicación también debería cancelar la alarma previamente establecida con el AlarmAdapter, sin embarga dicha característica no fue implementada en esta versión de la aplicación. 

Además es de tener en cuenta las serias limitaciones que presenta el sistema cuando se pierde la conexión bluetooth entre el móvil y el simblee pues el primer dispositivo mencionado es el encargado de encender la alarma silenciosa en simblee.

### `turnOffAlarm()`

![enter image description here](https://i.imgur.com/7ebH0IA.png)

Escribimos una característica definida en el host encarga de gestionar el estado de la alarma (1 - prendido, 0 - apagado).

---

## Programando el host

A continuación, deberemos programar el host, para ello seguiremos la [guía de inicio rápido](https://cdn.sparkfun.com/datasheets/IoT/Simblee%20User%20Guide%20v2.05.pdf) de la plataforma Simblee.

Luego cargamos el siguiente [programa](https://github.com/jorovipe97/GattServerSimblee/blob/master/LedButtonSimblee.ino) en el simblee.

---

## Programando el cliente

Lo primero que deberemos hacer en nuestra aplicación es establecer los permisión necesarios en el `AndroidManifest.xml`

    <uses-permission android:name="android.permission.BLUETOOTH" />    
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />    
    <uses-permission android:name="android.permission.WAKE_LOCK" />

Luego, escribimos los siguientes colores en `values/colors.xml`

    <color name="colorPrimary">#880e4f</color>
    <color name="colorPrimaryDark">#bc477b</color>
    <color name="colorAccent">#560027</color>

Ahora escribiremos la siguiente layout

![enter image description here](https://i.imgur.com/fDSVxLS.png)

en `layout/activity_main.xml` usando la siguiente [estructura xml](https://github.com/romualdo97/GattClientAlarm/blob/master/app/src/main/res/layout/activity_main.xml):

### Programando `setAlarm()`

> Para revisar declaraciones e implementaciones de simbolos no declarados previamente en este tutorial por favor dirijase al [MainActivity.java](https://github.com/romualdo97/GattClientAlarm/blob/master/app/src/main/java/com/romualdo/ble/gattclient/MainActivity.java) +

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

### Programando `cancelAlarm()`

	public void disconnect(View view) {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
    }

### Programando `turnOffAlarm()`

	// MainActivity.onCreate()
	btnOnOff = (Button) findViewById(R.id.btnOnOff);
	btnOnOff.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			writeCharacteristic(false);
		}
	});
	// ==========================================================
	// MainActivity.writeCharacteristic()
	private boolean writeCharacteristic(boolean data) {
	
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
		} else {
			val[0] = (byte) 0;
		}

		ledCharacteristic.setValue(val);
		mBluetoothGatt.writeCharacteristic(ledCharacteristic);
		if (SHOW_TOAST) { Toast.makeText(this, "Written in led service, val = " + val[0], Toast.LENGTH_SHORT).show(); }
		return true;
	}

---

## Otros recursos

> - [How to Create Android BLE Application Faster and Easier?](http://www.instructables.com/id/How-to-create-Android-BLE-application-faster-and-e/)
> - [Communicating with Bluetooth Low Energy devices](http://nilhcem.com/android-things/bluetooth-low-energy)
> - [how to write characteristics?](https://stackoverflow.com/questions/20043388/working-with-ble-android-4-3-how-to-write-characteristics)