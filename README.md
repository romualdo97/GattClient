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

Además es de tener en cuenta las serias limitaciones que presenta el sistema cuando se pierde la conexión bluetooth entre el móvil y el simblee pues es el primer dispositivo mencionado el encargado de encender la alarma silenciosa en simblee.

### `turnOffAlarm()`

![enter image description here](https://i.imgur.com/7ebH0IA.png)

Escribimos una característica definida en el host encarga de gestionar el estado de la alarma (1 - prendido, 0 - apagado)....