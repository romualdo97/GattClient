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

en `layout/activity_main.xml` usando la siguiente estructura:

    <?xml version="1.0" encoding="utf-8"?>
    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    tools:context="com.romualdo.ble.gattclient.MainActivity"
    android:weightSum="1"
    android:orientation="vertical">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center"
        android:orientation="horizontal">

        <Button
            android:id="@+id/buttonConnect"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:onClick="startClient"
            android:text="Set alarm"
            tools:layout_editor_absoluteX="76dp"
            tools:layout_editor_absoluteY="231dp" />

        <Button
            android:id="@+id/buttonDisconnect"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:enabled="false"
            android:onClick="disconnect"
            android:text="Cancel alarm"
            tools:layout_editor_absoluteX="200dp"
            tools:layout_editor_absoluteY="231dp" />

    </LinearLayout>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center">


        <TextView
            android:id="@+id/btnStatus"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Button up"
            android:visibility="invisible"
            android:textSize="24sp" />

    </LinearLayout>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center">


        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Alarm setted for:"
            android:textSize="18sp" />

    </LinearLayout>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center"
        android:orientation="horizontal">

        <TextView
            android:id="@+id/clockView"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="20:23"
            android:textSize="46sp"
            android:textStyle="bold" />

    </LinearLayout>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center">


        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="of today"
            android:textSize="18sp" />

    </LinearLayout>

    <LinearLayout
        android:layout_width="121dp"
        android:layout_height="wrap_content"
        android:layout_weight="0.11">

    </LinearLayout>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center">

        <Button
            android:id="@+id/btnOnOff"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Turn off"
            android:textSize="24sp" />

    </LinearLayout>

    </LinearLayout>
